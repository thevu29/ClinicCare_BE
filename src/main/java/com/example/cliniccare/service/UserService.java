package com.example.cliniccare.service;

import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.Role;
import com.example.cliniccare.model.User;
import com.example.cliniccare.pagination.PaginationQuery;
import com.example.cliniccare.pagination.PaginationService;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseStorageService firebaseStorageService;
    private final PaginationService paginationService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            DoctorProfileRepository doctorProfileRepository,
            PasswordEncoder passwordEncoder,
            FirebaseStorageService firebaseStorageService,
            PaginationService paginationService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.firebaseStorageService = firebaseStorageService;
        this.paginationService = paginationService;
    }

    public PaginationResponse<List<UserDTO>> getUsers(
            PaginationQuery paginationQuery,
            String search
    ) {
        Pageable pageable = paginationService.getPageable(paginationQuery);
        Page<User> users = userRepository.findByDeleteAtIsNullAndNameContainingOrPhoneContaining(search, search, pageable);
        int totalPage = paginationService.getTotalPages(users.getTotalElements(), paginationQuery.size);
        long totalElements = users.getTotalElements();

        return new PaginationResponse<>(
                true,
                "Get users successfully",
                users.map(UserDTO::new).getContent(),
                paginationQuery.page,
                paginationQuery.size,
                totalPage,
                totalElements
        );
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
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            user.setImage(firebaseStorageService.uploadImage(userDTO.getImage()));
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

        if (userDTO.getName() != null && !userDTO.getName().isEmpty()) {
            user.setName(userDTO.getName());
        }
        if (userDTO.getPhone() != null && !userDTO.getPhone().isEmpty()) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            user.setImage(firebaseStorageService.uploadImage(userDTO.getImage()));
        }

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findByUserIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole().getName().equals("Admin")) {
            throw new BadRequestException("Cannot delete admin");
        }

        if (user.getRole().getName().equals("Doctor")) {
            DoctorProfile doctor = doctorProfileRepository.findByUser_UserIdAndDeleteAtIsNull(user.getUserId())
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            doctor.setDeleteAt(new Date());
            doctorProfileRepository.save(doctor);
        }

        user.setDeleteAt(new Date());
        userRepository.save(user);
    }
}
