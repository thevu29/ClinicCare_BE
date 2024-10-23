package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    private Role role;

    private String name;

    private String email;

    private String password;
    
    private String phone;

    private String image;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "delete_at")
    private Date deleteAt;

    @OneToOne(mappedBy = "user")
    private DoctorProfile doctorProfile;

    @OneToMany(mappedBy = "patient")
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointmentList;

    @OneToMany(mappedBy = "patient")
    private List<MedicalRecord> medicalRecordList;

    @OneToMany(mappedBy = "user")
    private List<Notification> notificationList;

    @OneToMany(mappedBy = "patient")
    private List<Payment> paymentList;

    @PrePersist
    protected void onCreate() {
        createAt = new Date();
    }
}
