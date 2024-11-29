package com.example.cliniccare.service;

import com.example.cliniccare.entity.Role;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.entity.UserInfoDetails;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailAndDeleteAtIsNull(username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User findOrCreateUserFromOAuth(String email, String name) {
        return userRepository.findByEmailAndDeleteAtIsNull(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(null);

                    Role userRole = roleRepository.findByNameIgnoreCase("user")
                            .orElseThrow(() -> new RuntimeException("Default role not found"));
                    newUser.setRole(userRole);

                    return userRepository.save(newUser);
                });
    }
}
