package com.example.cliniccare.repository;

import com.example.cliniccare.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByEmailAndOtpAndExpireAtAfter(
            String email, String otp, java.time.LocalDateTime expireAt);
}
