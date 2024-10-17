package com.example.cliniccare.service;

import com.example.cliniccare.dto.DoctorProfileDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorProfileService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorProfileService.class);

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Autowired
    public DoctorProfileService(
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository
    ) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public List<DoctorProfileDTO> getDoctorProfile() {
        List<DoctorProfile> doctorProfile = doctorProfileRepository.findByDeleteAtIsNull();
        return doctorProfile.stream().map(DoctorProfileDTO::new).toList();
    }

    public DoctorProfileDTO getDoctorProfileById(UUID id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        return new DoctorProfileDTO(doctorProfile);
    }

    public DoctorProfileDTO createDoctorProfile(DoctorProfileDTO doctorProfileDTO) {
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setSpecialty(doctorProfileDTO.getSpecialty());
        doctorProfile.setCreateAt(Timestamp.valueOf(LocalDateTime.now()));
        doctorProfile.setDeleteAt(doctorProfileDTO.getDeleteAt());

        User user = userRepository.findById(doctorProfileDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        doctorProfile.setUser(user);

        DoctorProfile savedDoctorProfile = doctorProfileRepository.save(doctorProfile);
        return new DoctorProfileDTO(savedDoctorProfile);
    }

    public DoctorProfileDTO updateDoctorProfile(UUID id, DoctorProfileDTO doctorProfileDTO) {
        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        if (doctorProfileDTO.getSpecialty() != null) {
            doctorProfile.setSpecialty(doctorProfileDTO.getSpecialty());
        }

        doctorProfile.setCreateAt(Timestamp.valueOf(LocalDateTime.now()));
        doctorProfile.setDeleteAt(doctorProfileDTO.getDeleteAt());

        User user = userRepository.findById(doctorProfileDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        doctorProfile.setUser(user);

        DoctorProfile updatedDoctorProfile = doctorProfileRepository.save(doctorProfile);
        return new DoctorProfileDTO(updatedDoctorProfile);
    }

    public void deleteDoctorProfile(UUID id) {
        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        doctorProfile.setDeleteAt(new Date());
        doctorProfileRepository.save(doctorProfile);
    }
}
