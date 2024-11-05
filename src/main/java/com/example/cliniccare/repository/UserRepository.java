package com.example.cliniccare.repository;

import com.example.cliniccare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Page<User> findByDeleteAtIsNull(Pageable pageable);
    Page<User> findByDeleteAtIsNullAndNameContainingOrPhoneContaining(String name, String phone, Pageable pageable);
    Optional<User> findByUserIdAndDeleteAtIsNull(UUID userId);
    Optional<User> findByEmailAndDeleteAtIsNull(String email);
    Boolean existsByEmailAndDeleteAtIsNull(String email);
    Boolean existsByPhoneAndDeleteAtIsNull(String phone);
}
