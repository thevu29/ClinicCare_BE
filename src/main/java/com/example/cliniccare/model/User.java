package com.example.cliniccare.model;

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
    private LocalDateTime createAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @OneToOne(mappedBy = "user")
    private DoctorProfile doctorProfile;

    @OneToMany(mappedBy = "patient")
    @ToString.Exclude
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "patient")
    @ToString.Exclude
    private List<Appointment> appointmentList;

    @OneToMany(mappedBy = "patient")
    @ToString.Exclude
    private List<MedicalRecord> medicalRecordList;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Notification> notificationList;

    @OneToMany(mappedBy = "patient")
    @ToString.Exclude
    private List<Payment> paymentList;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
