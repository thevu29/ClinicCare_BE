package com.example.cliniccare.repository;

import com.example.cliniccare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByPatientUserId(UUID patientId);
    List<Appointment> findByScheduleDoctorDoctorProfileId(UUID doctorId);
}
