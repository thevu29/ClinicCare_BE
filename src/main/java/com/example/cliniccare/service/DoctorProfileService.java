package com.example.cliniccare.service;

import com.example.cliniccare.dto.DoctorProfileDTO;
import com.example.cliniccare.dto.DoctorProfileFormDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorProfileService {
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;
    private final PaginationService paginationService;

    @Autowired
    public DoctorProfileService(
            DoctorProfileRepository doctorProfileRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            FirebaseStorageService firebaseStorageService,
            PaginationService paginationService,
            PasswordEncoder passwordEncoder
    ) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
        this.paginationService = paginationService;

    }

    public PaginationResponse<List<DoctorProfileDTO>> getDoctorProfiles(
            PaginationDTO paginationQuery,
            String search
    ) {
        Pageable pageable = paginationService.getPageable(paginationQuery);
        Page<DoctorProfile> doctorProfiles = doctorProfileRepository
                .findByDeleteAtIsNullAndSpecialtyContaining(search, pageable);
        int totalPage = paginationService.getTotalPages(doctorProfiles.getTotalElements(), paginationQuery.size);
        long totalElements = doctorProfiles.getTotalElements();
        return new PaginationResponse<>(
                true,
                "Get doctors successfully",
                doctorProfiles.map(doctorProfile -> new DoctorProfileDTO(
                        doctorProfile,
                        userRepository.findByUserIdAndDeleteAtIsNull(doctorProfile.getUser().getUserId())
                                .orElseThrow(() -> new NotFoundException("User not found"))
                )).getContent(),
                paginationQuery.page,
                paginationQuery.size,
                totalPage,
                totalElements
        );
    }


    public DoctorProfileDTO getDoctorProfileById(UUID id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        User userProfile = userRepository.findByUserIdAndDeleteAtIsNull(doctorProfile.getUser().getUserId())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        return new DoctorProfileDTO(doctorProfile, userProfile);
    }

    public DoctorProfileDTO createDoctorProfile(DoctorProfileFormDTO doctorProfileDTO)
            throws IOException {
        if (userRepository.existsByEmailAndDeleteAtIsNull(doctorProfileDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (doctorProfileDTO.getImage().isEmpty()) {
            throw new BadRequestException("Image is required");
        }

        User user = new User();
        user.setName(doctorProfileDTO.getName());
        user.setEmail(doctorProfileDTO.getEmail());
        user.setPhone(doctorProfileDTO.getPhone());
        user.setPassword(passwordEncoder.encode(doctorProfileDTO.getPassword()));
        user.setImage(firebaseStorageService.uploadImage(doctorProfileDTO.getImage()));
        user.setRole(roleRepository.findByNameIgnoreCase("Doctor")
                .orElseThrow(() -> new NotFoundException("Doctor role not found")));

        User savedUser = userRepository.save(user);

        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setSpecialty(doctorProfileDTO.getSpecialty());
        doctorProfile.setUser(savedUser);

        DoctorProfile savedDoctorProfile = doctorProfileRepository.save(doctorProfile);

        return new DoctorProfileDTO(savedDoctorProfile, savedUser);
    }

    public DoctorProfileDTO updateDoctorProfile(UUID id, DoctorProfileFormDTO doctorProfileDTO)
            throws IOException {

        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        User user = userRepository.findByUserIdAndDeleteAtIsNull(doctorProfile.getUser().getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (doctorProfileDTO.getName() != null && !doctorProfileDTO.getName().isEmpty()) {
            user.setName(doctorProfileDTO.getName());
        }
        if (doctorProfileDTO.getPhone() != null && !doctorProfileDTO.getPhone().isEmpty()) {
            user.setPhone(doctorProfileDTO.getPhone());
        }
        if (doctorProfileDTO.getImage() != null && !doctorProfileDTO.getImage().isEmpty()) {
            user.setImage(firebaseStorageService.uploadImage(doctorProfileDTO.getImage()));
        }
        if (doctorProfileDTO.getSpecialty() != null) {
            doctorProfile.setSpecialty(doctorProfileDTO.getSpecialty());
        }

        User updatedUser = userRepository.save(user);

        DoctorProfile updatedDoctorProfile = doctorProfileRepository.save(doctorProfile);

        return new DoctorProfileDTO(updatedDoctorProfile, updatedUser);
    }

    public void deleteDoctorProfile(UUID id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        User user = userRepository.findByUserIdAndDeleteAtIsNull(doctorProfile.getUser().getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        doctorProfile.setDeleteAt(LocalDateTime.now());
        doctorProfileRepository.save(doctorProfile);

        user.setDeleteAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
