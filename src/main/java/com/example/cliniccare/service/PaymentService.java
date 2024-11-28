package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.PaymentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Payment;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.PaymentRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import com.example.cliniccare.utils.NumberQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class PaymentService {
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
            System.out.println(status);
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
            String status, String method, String date, String price
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
        List<PaymentDTO> paymentDTOs = payments.map(PaymentDTO::new).toList();
        int take = payments.getNumberOfElements();

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
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
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

        return new PaymentDTO(savedPayment);
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

    public double getMonthlyProfit(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month. Please provide a value between 1 and 12.");
        }
        if (year > LocalDate.now().getYear()) {
            throw new BadRequestException("Invalid year. Please provide a valid year.");
        }

        Double profit = paymentRepository.calculateMonthlyProfit(month, year);
        return profit != null ? profit : 0.0;
    }
}
