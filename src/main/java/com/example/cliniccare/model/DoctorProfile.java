package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
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
    private Date deleteAt;

    @OneToMany(mappedBy = "doctor")
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "doctor")
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "doctor")
    private List<MedicalRecord> medicalRecordList;
}
