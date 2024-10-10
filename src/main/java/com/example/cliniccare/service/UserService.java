package com.example.cliniccare.service;

import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.exception.ResourceNotFoundException;
import com.example.cliniccare.model.Role;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.utils.Formatter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getUsers() {
        try {
            List<User> users = userRepository.findByDeleteAtIsNull();
            return users.stream().map(UserDTO::new).toList();
        } catch (Exception e) {
            logger.error("Failed to get users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get users", e);
        }
    }

    public UserDTO getUserById(UUID id) {
        try {
            User user = userRepository.findByUserIdAndDeleteAtIsNull(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return new UserDTO(user);
        } catch (Exception e) {
            logger.error("Failed to get user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public UserDTO createUser(UserFormDTO userDTO) {
        try {
            User user = new User();
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());

            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            user.setPassword(encodedPassword);

            UUID roleId = Formatter.fromHexString(userDTO.getRoleId());

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);

            User savedUser = userRepository.save(user);
            return new UserDTO(savedUser);
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
}
