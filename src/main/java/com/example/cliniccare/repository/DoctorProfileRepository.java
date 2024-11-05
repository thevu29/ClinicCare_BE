package com.example.cliniccare.repository;

import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    Page<DoctorProfile> findByDeleteAtIsNullAndSpecialtyContaining(String specialty, Pageable pageable);
    List<DoctorProfile> findByDeleteAtIsNull();
    Optional<DoctorProfile> findByDoctorProfileIdAndDeleteAtIsNull(UUID doctorProfileId);
    Optional<DoctorProfile> findByUser_UserIdAndDeleteAtIsNull(UUID userId);
}