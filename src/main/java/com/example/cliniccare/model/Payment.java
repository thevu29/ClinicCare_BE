package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "payments")
public class Payment {
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

    private Date date;

    @Column(name = "total_price")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}

enum PaymentStatus {
    PENDING,
    PAID,
    CANCELLED
}
