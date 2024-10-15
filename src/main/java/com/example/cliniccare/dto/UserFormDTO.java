package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.CreateUserFormGroup;
import com.example.cliniccare.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Data
public class UserFormDTO {
    @NotBlank(message = "Name is required", groups = {CreateUserFormGroup.class})
    private String name;

    @NotBlank(message = "Email is required", groups = {CreateUserFormGroup.class})
    @Email(message = "Invalid email format", groups = {CreateUserFormGroup.class})
    private String email;

    @ValidPhone(message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Password is required", groups = {CreateUserFormGroup.class})
    private String password;

    private MultipartFile image;

    @NotNull(message = "Role is required", groups = {CreateUserFormGroup.class})
    private UUID roleId;

    private Date createAt;
}
