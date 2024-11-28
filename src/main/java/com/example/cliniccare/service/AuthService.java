package com.example.cliniccare.service;

import com.example.cliniccare.entity.UserInfoDetails;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailAndDeleteAtIsNull(username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
