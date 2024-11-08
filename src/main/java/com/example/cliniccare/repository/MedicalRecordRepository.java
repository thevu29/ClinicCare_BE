package com.example.cliniccare.repository;

import com.example.cliniccare.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID>, JpaSpecificationExecutor<MedicalRecord> {
    Optional<MedicalRecord> findByMedicalRecordIdAndDeleteAtIsNull(UUID medicalRecordId);
}
