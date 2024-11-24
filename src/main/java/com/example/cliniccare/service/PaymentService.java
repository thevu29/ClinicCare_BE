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
import com.example.cliniccare.utils.NumberToWords;
import com.example.cliniccare.utils.PriceQueryParser;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.PdfEncodings;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public PaginationResponse<List<PaymentDTO>> getPayments(
            PaginationDTO paginationDTO, UUID patientId, UUID serviceId,
            String status, String method, String date, String price
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Payment> spec = Specification.where(null);

        if (patientId != null) {
            User patient = patientRepository.findByUserIdAndDeleteAtIsNull(patientId)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            spec = spec.and((root, query, cb) -> cb.equal(root.get("patient").get("userId"), patient.getUserId()));
        }
        if (serviceId != null) {
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            spec = spec.and((root, query, cb) -> cb.equal(root.get("service").get("serviceId"), service.getServiceId()));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), getPaymentStatus(status)));
        }
        if (method != null && !method.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("method"), getPaymentMethod(method)));
        }
        if (date != null && !date.isEmpty()) {
            DateQueryParser<Payment> dateQueryParser = new DateQueryParser<>(date, "date");
            spec = spec.and(dateQueryParser.createDateSpecification());
        }
        if (price != null && !price.isEmpty()) {
            PriceQueryParser<Payment> priceQueryParser = new PriceQueryParser<>(price, "totalPrice");
            spec = spec.and(priceQueryParser.createPriceSpecification());
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

    public List<PaymentDTO> getPatientPayments(UUID patientId) {
        User patient = patientRepository.findByUserIdAndDeleteAtIsNull(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        List<Payment> payments = paymentRepository.findAllByPatient_UserIdOrderByDateDesc(patient.getUserId());
        return payments.stream().map(PaymentDTO::new).toList();
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

    @Transactional
    public PaymentDTO exportPDFPayment(UUID paymentId, String url) {
        // Find payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        // Find service
        Service service = payment.getService();

        // Find patient
        User patient = payment.getPatient();

        // Destination file
        String dest = url != null && !url.isEmpty() ? url : "MedicalInvoice.pdf";

        // check location
        if (!dest.endsWith(".pdf")) {
            throw new BadRequestException("Invalid file format");
        }
        try {
            File file = new File(dest);

            // Check if file exists
            if (file.exists()) {
                throw new BadRequestException("File already exists");
            }



            // Create PDF
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/times.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);

            // Title
            Paragraph title = new Paragraph("HÓA ĐƠN KHÁM BỆNH")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(title);

            // Clinic Information
            Paragraph clinicInfo = new Paragraph("PHÒNG KHÁM TƯ NHÂN\n" +
                    "Địa chỉ: An Dương Vương, Quận 5, TPHCM\n" +
                    "Điện thoại:0123456789 \nWebsite:ciniccare.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(clinicInfo);



            // Invoice Table
            Paragraph titlePayment = new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(titlePayment);

            // Customer Information
            Paragraph customerInfo = new Paragraph("""
                    Tên khách hàng: %s\n
                    Số điện thoại: %s\n""".formatted(patient.getName(), patient.getPhone()))
                    .setMarginTop(20)
                    .setFontSize(12);
            document.add(customerInfo);

            // Name of Service and method of payment and price
            Paragraph serviceName = new Paragraph("""
                    Dịch vụ: %s\n
                    Phương thức thanh toán: %s\n
                    Thành tiền: %s vnđ (%s)\n
                    Đã thanh toán.\n""".formatted(service.getName(), payment.getMethod(), payment.getTotalPrice(), NumberToWords.convert(payment.getTotalPrice())))
                    .setMarginTop(20)
                    .setFontSize(12);
            document.add(serviceName);

            // Format date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Ngày' d 'tháng' M 'năm' yyyy");
            String formattedDate = payment.getDate().format(formatter);

            // Footer
            Paragraph footer = new Paragraph("Thành phố Hồ Chí Minh, "+ formattedDate +"\n\n" +
                    "Bệnh nhân                          Nhân viên thu tiền                    Bác sĩ điều trị\n" +
                    "(Ký, họ tên)                        (Ký, họ tên)                                  (Ký, họ tên)\n\n\n\n\n\n\n")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30)
                    .setFontSize(12);
            document.add(footer);

            // Generate 1D Barcode (Code 128)
            Barcode128 barcode = new Barcode128(pdf);
            barcode.setCode(payment.getPaymentId().toString());
            Image barcodeImage = new Image(barcode.createFormXObject(pdf));
            barcodeImage.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            document.add(barcodeImage);

            // Close document
            document.close();
            return new PaymentDTO(payment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Error creating PDF Invoice");
        }


    }
}
