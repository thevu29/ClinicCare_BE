package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payments")
public class Payment {
    public enum PaymentStatus {
        PENDING,
        PAID,
        CANCELLED
    }

    public enum PaymentMethod {
        CASH,
        BANKING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "service_id")
    private Service service;

    private LocalDateTime date;

    @Column(name = "total_price")
    private double totalPrice;

    private PaymentStatus status;

    private PaymentMethod method;

    @PrePersist
    protected void onCreate() {
        status = PaymentStatus.PENDING;
        date = LocalDateTime.now();
    }
}
