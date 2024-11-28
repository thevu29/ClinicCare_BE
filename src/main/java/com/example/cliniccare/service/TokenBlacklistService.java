package com.example.cliniccare.service;

import com.example.cliniccare.entity.TokenBlacklist;
import com.example.cliniccare.repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            TokenBlacklist tokenBlacklist = new TokenBlacklist();
            tokenBlacklist.setToken(token);
            tokenBlacklist.setInvalidatedAt(LocalDateTime.now());
            tokenBlacklistRepository.save(tokenBlacklist);
        }
    }
}
