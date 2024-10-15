package com.example.cliniccare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_id")
    private UUID serviceId;

    private String name;

    private String description;

    private double price;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "delete_at")
    private Date deleteAt;

    @OneToMany(mappedBy = "service")
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "service")
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "service")
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "service")
    private List<MedicalRecord> medicalRecordList;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "promotion_id", referencedColumnName = "promotion_id")
    private Promotion promotion;
}
