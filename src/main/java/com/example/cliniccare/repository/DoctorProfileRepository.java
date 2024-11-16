package com.example.cliniccare.repository;

import com.example.cliniccare.model.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    @Query("SELECT dp FROM DoctorProfile dp " +
            "WHERE dp.deleteAt IS NULL " +
            "AND dp.user.deleteAt IS NULL " +
            "AND (dp.user.name LIKE %:search% " +
            "OR dp.user.email LIKE %:search% " +
            "OR dp.specialty LIKE %:search%)")
    Page<DoctorProfile> findAllDoctorProfiles(@Param("search") String search, Pageable pageable);
    Page<DoctorProfile> findAllByDeleteAtIsNull(Pageable pageable);
    List<DoctorProfile> findAllByDeleteAtIsNull();
    Optional<DoctorProfile> findByDoctorProfileIdAndDeleteAtIsNull(UUID doctorProfileId);
    Optional<DoctorProfile> findByUser_UserIdAndDeleteAtIsNull(UUID userId);
}
