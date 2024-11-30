package com.example.cliniccare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "medical_records")
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "medical_record_id")
    private UUID medicalRecordId;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_profile_id", referencedColumnName = "doctor_profile_id")
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "service_id")
    private Service service;

    private String description;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @PrePersist
    public void onCreate() {
        this.createAt = LocalDateTime.now();
    }
}
