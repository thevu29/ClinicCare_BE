package com.example.cliniccare.repository;

import com.example.cliniccare.model.DoctorProfile;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    List<DoctorProfile> findByDeleteAtIsNull();
    Optional<DoctorProfile> findByDoctorProfileIdAndDeleteAtIsNull(UUID doctorProfileId);
    boolean existsBySpecialtyAndDeleteAtIsNull(@NotBlank(message = "Specialty is required") String specialty);
}