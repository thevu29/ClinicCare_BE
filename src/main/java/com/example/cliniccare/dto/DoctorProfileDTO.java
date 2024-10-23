package com.example.cliniccare.dto;

import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DoctorProfileDTO {
    private UUID userId;
    private UUID doctorProfileId;
    private String name;
    private String email;
    private String phone;
    private String image;
    private String specialty;
    private LocalDateTime createAt;

    public DoctorProfileDTO() {
    }

    public DoctorProfileDTO(DoctorProfile doctorProfile, User user) {
        this.userId = user.getUserId();
        this.doctorProfileId = doctorProfile.getDoctorProfileId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.image = user.getImage();
        this.specialty = doctorProfile.getSpecialty();
        this.createAt = user.getCreateAt();
    }
}
