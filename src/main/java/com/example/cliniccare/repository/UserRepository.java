package com.example.cliniccare.repository;

import com.example.cliniccare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByDeleteAtIsNull();
    Optional<User> findByUserIdAndDeleteAtIsNull(UUID userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Boolean existsByEmailAndDeleteAtIsNull(String email);
    Boolean existsByPhoneAndDeleteAtIsNull(String phone);
}
