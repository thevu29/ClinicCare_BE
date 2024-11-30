package com.example.cliniccare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "email_verification")
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_verification_id")
    private UUID emailVerificationId;

    @Column(name = "email")
    private String email;

    @Column(name = "otp")
    private String otp;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @PrePersist
    public void onCreate() {
        createAt = LocalDateTime.now();
        expireAt = createAt.plusMinutes(5);
    }
}
