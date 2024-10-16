package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.UserFormGroup;
import com.example.cliniccare.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Data
public class UserFormDTO {
    @NotBlank(message = "Name is required", groups = {UserFormGroup.Create.class})
    private String name;

    @NotBlank(message = "Email is required", groups = {UserFormGroup.Create.class})
    @Email(message = "Invalid email format", groups = {UserFormGroup.Create.class})
    private String email;

    @ValidPhone(message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Password is required", groups = {UserFormGroup.Create.class})
    private String password;

    private MultipartFile image;

    @NotNull(message = "Role is required", groups = {UserFormGroup.Create.class})
    private UUID roleId;

    private Date createAt;
}