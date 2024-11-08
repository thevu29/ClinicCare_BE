package com.example.cliniccare.dto;

import com.example.cliniccare.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String image;
    private String role;
    private String specialty;
    private LocalDateTime createAt;

    public UserDTO() {}

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.image = user.getImage();
        this.role = user.getRole().getName();
        this.specialty = user.getDoctorProfile() != null ? user.getDoctorProfile().getSpecialty() : null;
        this.createAt = user.getCreateAt();
    }
}
