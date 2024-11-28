package com.example.cliniccare.repository;

import com.example.cliniccare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByPatientUserId(UUID patientId);
    List<Appointment> findByScheduleDoctorDoctorProfileId(UUID doctorId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE MONTH(a.date) = :month AND YEAR(a.date) = :year")
    long countAppointmentsByMonth(@Param("month") int month, @Param("year") int year);
}
