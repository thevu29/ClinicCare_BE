package com.example.cliniccare.repository;

import com.example.cliniccare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByPatientUserId(UUID patientId);
    List<Appointment> findByScheduleDoctorDoctorProfileId(UUID doctorId);
}
