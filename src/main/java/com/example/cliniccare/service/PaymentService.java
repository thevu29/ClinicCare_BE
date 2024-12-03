package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.PaymentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.Payment;
import com.example.cliniccare.entity.Service;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.repository.PaymentRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import com.example.cliniccare.utils.NumberQueryParser;
import com.example.cliniccare.utils.VNPayUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@org.springframework.stereotype.Service
public class PaymentService {
    @Value("${vnp_TmnCode}")
    private String vnp_TmnCode;

    @Value("${vnp_HashSecret}")
    private String secretKey;

    @Value("${vnp_PayUrl}")
    private String vnp_PayUrl;

    @Value("${vnp_ReturnUrl}")
    private String vnp_ReturnUrl;

    private final PaymentRepository paymentRepository;
    private final UserRepository patientRepository;
    private final ServiceRepository serviceRepository;
    private final PaginationService paginationService;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            UserRepository patientRepository,
            ServiceRepository serviceRepository,
            PaginationService paginationService
    ) {
        this.paymentRepository = paymentRepository;
        this.patientRepository = patientRepository;
        this.serviceRepository = serviceRepository;
        this.paginationService = paginationService;
    }

    private Payment.PaymentStatus getPaymentStatus(String status) {
        try {
            return status != null && !status.isEmpty()
                    ? Payment.PaymentStatus.valueOf(status.toUpperCase())
                    : Payment.PaymentStatus.PENDING;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid payment status");
        }
    }

    private Payment.PaymentMethod getPaymentMethod(String method) {
        try {
            return Payment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid payment method: CASH or BANKING");
        }
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentDTO::new)
                .toList();
    }

    public PaginationResponse<List<PaymentDTO>> getPayments(
            PaginationDTO paginationDTO, UUID patientId, UUID serviceId,
            String search, String status, String method, String date, String price
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Payment> spec = Specification.where(null);

        if (patientId != null) {
            User patient = patientRepository.findByUserIdAndDeleteAtIsNull(patientId)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("patient").get("userId"), patient.getUserId()));
        }
        if (serviceId != null) {
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("service").get("serviceId"), service.getServiceId()));
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchLowercase = search.toLowerCase();
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("patient").get("name")), "%" + searchLowercase + "%"),
                            cb.like(cb.lower(root.get("service").get("name")), "%" + searchLowercase + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), getPaymentStatus(status)));
        }
        if (method != null && !method.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("method"), getPaymentMethod(method)));
        }
        if (date != null && !date.isEmpty()) {
            DateQueryParser<Payment> dateQueryParser = new DateQueryParser<>(date, "date");
            spec = spec.and(dateQueryParser.createDateSpecification());
        }
        if (price != null && !price.isEmpty()) {
            NumberQueryParser<Payment> numberQueryParser = new NumberQueryParser<>(price, "totalPrice");
            spec = spec.and(numberQueryParser.createPriceSpecification());
        }

        Page<Payment> payments = paymentRepository.findAll(spec, pageable);

        int totalPages = payments.getTotalPages();
        long totalElements = payments.getTotalElements();
        int take = payments.getNumberOfElements();

        List<PaymentDTO> paymentDTOs = payments.map(PaymentDTO::new).toList();

        return new PaginationResponse<>(
                true,
                "Payments retrieved successfully",
                paymentDTOs,
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
    }

    public PaymentDTO getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        return new PaymentDTO(payment);
    }

    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO, HttpServletRequest req) {
        User patient = patientRepository.findById(paymentDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        Service service = serviceRepository.findById(paymentDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setService(service);
        payment.setMethod(getPaymentMethod(paymentDTO.getMethod()));

        double price = service.getPromotion() != null && service.getPromotion().getExpireAt().isBefore(LocalDate.now())
                ? service.getPrice() * (100 - service.getPromotion().getDiscount()) / 100
                : service.getPrice();

        payment.setTotalPrice(price);

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getMethod() == Payment.PaymentMethod.BANKING) {
            String paymentUrl = createPaymentUrl(price, savedPayment.getPaymentId(), req);
            PaymentDTO newPaymentDTO = new PaymentDTO(savedPayment);
            newPaymentDTO.setPaymentUrl(paymentUrl);

            return newPaymentDTO;
        } else {
            savedPayment.setStatus(Payment.PaymentStatus.PAID);
            return new PaymentDTO(savedPayment);
        }
    }

    @Transactional
    public PaymentDTO changePaymentStatus(UUID paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new BadRequestException("Payment already paid");
        }

        payment.setStatus(getPaymentStatus(status));

        Payment updatedPayment = paymentRepository.save(payment);

        return new PaymentDTO(updatedPayment);
    }

    private String createPaymentUrl(double price, UUID paymentId, HttpServletRequest req) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = (long) (price * 100L);
        String bankCode = "NCB";

        String vnp_TxnRef = paymentId.toString();
        String vnp_IpAddr = VNPayUtils.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtils.hmacSHA512(secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnp_PayUrl + "?" + queryUrl;
    }

    @Transactional
    public Map<String, Object> checkVNPayResponse(Map<String, String> params) {
        Map<String, String> fields = new HashMap<>();
        Map<String, Object> response = new HashMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String fieldName = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = params.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = VNPayUtils.hashAllFields(fields, secretKey);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(params.get("vnp_TransactionStatus"))) {
                response.put("message", "Payment Successful!");
                response.put("success", true);

                changePaymentStatus(UUID.fromString(params.get("vnp_TxnRef")), "PAID");
            } else {
                response.put("message", "Payment Failed!");
                response.put("success", false);

                changePaymentStatus(UUID.fromString(params.get("vnp_TxnRef")), "CANCELLED");
            }
        } else {
            response.put("message", "Invalid signature!");
            response.put("success", false);
        }
        return response;
    }
}
