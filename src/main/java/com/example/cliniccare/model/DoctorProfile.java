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
@Table(name = "doctor_profiles")
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doctor_profile_id")
    private UUID doctorProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private String specialty;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @OneToMany(mappedBy = "doctor")
    @ToString.Exclude
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "doctor")
    @ToString.Exclude
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "doctor")
    @ToString.Exclude
    private List<MedicalRecord> medicalRecordList;

    private LocalDateTime createAt;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
