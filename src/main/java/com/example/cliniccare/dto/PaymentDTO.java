package com.example.cliniccare.dto;

import com.example.cliniccare.entity.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentDTO {
    private UUID paymentId;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private String patientName;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    private String serviceName;

    private LocalDateTime date;

    private double totalPrice;

    private String status;

    @NotBlank(message = "Method is required")
    private String method;

    public PaymentDTO() {
    }

    public PaymentDTO(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.patientId = payment.getPatient().getUserId();
        this.patientName = payment.getPatient().getName();
        this.serviceId = payment.getService().getServiceId();
        this.serviceName = payment.getService().getName();
        this.totalPrice = payment.getTotalPrice();
        this.date = payment.getDate();
        this.status = payment.getStatus().name();
        this.method = payment.getMethod().name();
    }
}
