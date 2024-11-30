package com.example.cliniccare.service;

import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.DoctorProfile;
import com.example.cliniccare.entity.Role;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public List<UserDTO> getAllPatient() {
        List<User> patients = userRepository.findAllByDeleteAtIsNullAndRole_Name("User");
        return patients.stream().map(UserDTO::new).toList();
    }

    public PaginationResponse<List<UserDTO>> getUsers(
            PaginationDTO paginationQuery,
            String search,
            UUID role
    ) {
        Pageable pageable = paginationService.getPageable(paginationQuery);

        List<String> searchParams = new ArrayList<>();
        if (StringUtils.isNotEmpty(search)) {
            searchParams.add("name");
            searchParams.add("phone");
            searchParams.add("email");
        }

        List<String> roleParams = new ArrayList<>();
        if (role != null) {
            if (!roleRepository.existsById(role)) {
                throw new NotFoundException("Role not found");
            }

            roleParams.add("role_id");
        }

        Page<User> users = userRepository.findByDeleteAtIsNullAndSearchParamsAndRoleParams(
                searchParams, roleParams, search, role, pageable
        );

        int totalPages = users.getTotalPages();
        long totalElements = users.getTotalElements();
        int take = users.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get users successfully",
                users.map(UserDTO::new).getContent(),
                paginationQuery.page,
                paginationQuery.size,
                take,
                totalPages,
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

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(role);

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            user.setImage(firebaseStorageService.uploadImage(userDTO.getImage()));
        }

        User savedUser = userRepository.save(user);

        if (role.getName().equalsIgnoreCase("doctor")) {
            DoctorProfile doctor = new DoctorProfile();

            if (userDTO.getSpecialty() == null || userDTO.getSpecialty().isEmpty()) {
                throw new BadRequestException("Specialty is required if user is doctor");
            }

            doctor.setSpecialty(userDTO.getSpecialty());
            doctor.setUser(user);
            doctorProfileRepository.save(doctor);

            savedUser.setDoctorProfile(doctor);
        }


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
            user.setImage(firebaseStorageService.updateImage(userDTO.getImage(), user.getImage()));
        }
        if (userDTO.getRoleId() != null) {
            Role role = roleRepository.findById(userDTO.getRoleId())
                    .orElseThrow(() -> new NotFoundException("Role not found"));

            if (
                    user.getRole().getName().equalsIgnoreCase("user") &&
                    !role.getName().equalsIgnoreCase("user")
            ) {
                throw new BadRequestException("Cannot change role of user");
            }
            if (
                    !user.getRole().getName().equalsIgnoreCase("user") &&
                    role.getName().equalsIgnoreCase("user")
            ) {
                throw new BadRequestException("Cannot change role to user");
            }

            user.setRole(role);
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

            doctor.setDeleteAt(LocalDateTime.now());
            doctorProfileRepository.save(doctor);
        }

        user.setEmail(null);
        user.setDeleteAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
