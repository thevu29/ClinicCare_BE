package com.example.cliniccare.repository;

import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    Optional<MedicalRecord> findByMedicalRecordIdAndDeleteAtIsNull(UUID medicalRecordId);
    List<MedicalRecord> findAllByPatient_UserId(UUID patientId);
    Page<MedicalRecord> findByDeleteAtIsNullAndDescriptionContaining(String description, Pageable pageable);
}
