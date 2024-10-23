package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "appointments")
public class Appointment {
    @Id
    @Column(name = "appointment_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID appointmentId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_phone")
    private String patientPhone;

    @OneToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private User patient;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancel_by", referencedColumnName = "user_id")
    private User cancelBy;

    @Column(name = "cancel_at")
    private LocalDateTime cancelAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }
}
