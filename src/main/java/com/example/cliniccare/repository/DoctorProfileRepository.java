package com.example.cliniccare.repository;

import com.example.cliniccare.model.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    Page<DoctorProfile> findAllByUserNameContainingOrUserEmailContainingOrSpecialtyAndDeleteAtIsNull(
            String name, String email, String specialty, Pageable pageable);
    Page<DoctorProfile> findAllBySpecialty(String specialty, Pageable pageable);
    Page<DoctorProfile> findAllByDeleteAtIsNull(Pageable pageable);
    Optional<DoctorProfile> findByDoctorProfileIdAndDeleteAtIsNull(UUID doctorProfileId);
    Optional<DoctorProfile> findByUser_UserIdAndDeleteAtIsNull(UUID userId);
}
