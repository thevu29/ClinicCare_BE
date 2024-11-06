package com.example.cliniccare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "services")
public class Service {
    public enum ServiceStatus {
        AVAILABLE,
        UNAVAILABLE,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_id")
    private UUID serviceId;

    private String name;

    private String description;

    private double price;

    private ServiceStatus status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @OneToMany(mappedBy = "service")
    @ToString.Exclude
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "service")
    @ToString.Exclude
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "service")
    @ToString.Exclude
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "service")
    @ToString.Exclude
    private List<MedicalRecord> medicalRecordList;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "promotion_id", referencedColumnName = "promotion_id")
    private Promotion promotion;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
    }
}