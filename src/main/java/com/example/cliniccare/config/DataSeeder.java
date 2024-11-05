package com.example.cliniccare.config;

import com.example.cliniccare.model.Role;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = new Role();
        if (roleRepository.count() == 0) {
            adminRole.setRoleId(UUID.randomUUID());
            adminRole.setName("Admin");

            Role userRole = new Role();
            userRole.setRoleId(UUID.randomUUID());
            userRole.setName("User");

            Role doctorRole = new Role();
            doctorRole.setRoleId(UUID.randomUUID());
            doctorRole.setName("Doctor");

            roleRepository.save(adminRole);
            roleRepository.save(userRole);
            roleRepository.save(doctorRole);
        }

        if (userRepository.findByEmailAndDeleteAtIsNull("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setUserId(UUID.randomUUID());
            admin.setName("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(adminRole);

            userRepository.save(admin);
        }
    }
}