package com.example.cliniccare.service;

import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Role;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseStorageService firebaseStorageService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            FirebaseStorageService firebaseStorageService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.firebaseStorageService = firebaseStorageService;
    }

    public List<UserDTO> getUsers() {
        List<User> users = userRepository.findByDeleteAtIsNull();
        return users.stream().map(UserDTO::new).toList();
    }

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findByUserIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new UserDTO(user);
    }

    public UserDTO createUser(UserFormDTO userDTO) throws IOException {
        if (userRepository.existsByEmailAndDeleteAtIsNull(userDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (userDTO.getPhone() != null &&
                !userDTO.getPhone().isEmpty() &&
                userRepository.existsByPhoneAndDeleteAtIsNull(userDTO.getPhone())
        ) {
            throw new BadRequestException("Phone already exists");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        user.setPassword(encodedPassword);

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            String imageUrl = firebaseStorageService.uploadImage(userDTO.getImage());
            user.setImage(imageUrl);
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public UserDTO updateUser(UUID id, UserFormDTO userDTO) throws IOException {
        User user = userRepository.findByUserIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDTO.getName() != null && !userDTO.getName().isEmpty()) user.setName(userDTO.getName());

        if (userDTO.getPhone() != null && !userDTO.getPhone().isEmpty()) {
            if (userRepository.existsByPhoneAndDeleteAtIsNull(userDTO.getPhone())) {
                throw new BadRequestException("Phone already exists");
            }

            user.setPhone(userDTO.getPhone());
        }

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            String imageUrl = firebaseStorageService.uploadImage(userDTO.getImage());
            user.setImage(imageUrl);
        }

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findByUserIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setDeleteAt(new Date());
        userRepository.save(user);
    }
}