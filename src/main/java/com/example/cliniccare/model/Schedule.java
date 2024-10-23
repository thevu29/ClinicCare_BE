package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "schedules")
public class Schedule {
    public enum ScheduleStatus {
        AVAILABLE,
        UNAVAILABLE,
        BOOKED,
        CANCELLED,
        COMPLETED
    }

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

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    private int duration;

    private ScheduleStatus status;

    @OneToOne(mappedBy = "schedule")
    private Appointment appointment;
}
