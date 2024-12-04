package com.example.cliniccare.repository;

import com.example.cliniccare.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID>, JpaSpecificationExecutor<DoctorProfile> {
    List<DoctorProfile> findAllByDeleteAtIsNull();
    Optional<DoctorProfile> findByDoctorProfileIdAndDeleteAtIsNull(UUID doctorProfileId);
    Optional<DoctorProfile> findByUser_UserIdAndDeleteAtIsNull(UUID userId);
}
