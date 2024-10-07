package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id")
    private UUID scheduleId;

    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "doctor_profile_id", referencedColumnName = "doctor_profile_id")
    private DoctorProfile doctor;

    @OneToOne(mappedBy = "schedule")
    private Appointment appointment;

    private Date date;

    @Enumerated(EnumType.STRING)
    private Status status;
}

