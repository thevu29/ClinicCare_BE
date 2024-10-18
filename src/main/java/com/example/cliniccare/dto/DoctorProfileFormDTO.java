package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.DoctorProfileGroup;
import com.example.cliniccare.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Data
public class DoctorProfileFormDTO {
    private UUID userId;

    private UUID doctorProfileId;

    @NotBlank(message = "Name is required", groups = {DoctorProfileGroup.Create.class})
    private String name;

    @NotBlank(message = "Email is required", groups = {DoctorProfileGroup.Create.class})
    @Email(message = "Email is invalid", groups = {DoctorProfileGroup.Create.class})
    private String email;

    @NotBlank(message = "Password is required", groups = {DoctorProfileGroup.Create.class})
    private String password;

    @ValidPhone(message = "Invalid phone number", groups = {DoctorProfileGroup.Create.class, DoctorProfileGroup.Update.class})
    private String phone;

    @NotNull(message = "Image is required", groups = {DoctorProfileGroup.Create.class})
    private MultipartFile image;

    @NotBlank(message = "Specialty is required", groups = {DoctorProfileGroup.Create.class})
    private String specialty;

    private Date createAt;
}
