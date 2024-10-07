package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "feedback_id")
    private UUID feedbackId;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_profile_id", referencedColumnName = "doctor_profile_id")
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "service_id")
    private Service service;

    private Date date;

    private String feedback;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "delete_at")
    private String deleteAt;

}
