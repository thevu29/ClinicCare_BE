package com.example.cliniccare.repository;

import com.example.cliniccare.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    TokenBlacklist findByToken(String token);
}
