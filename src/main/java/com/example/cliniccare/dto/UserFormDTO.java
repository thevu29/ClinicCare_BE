package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.CreateUserFormGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class UserFormDTO {
    @NotBlank(message = "Name is required", groups = {CreateUserFormGroup.class})
    private String name;

    @NotBlank(message = "Email is required", groups = {CreateUserFormGroup.class})
    @Email(message = "Invalid email format", groups = {CreateUserFormGroup.class})
    private String email;

    private String phone;

    @NotBlank(message = "Password is required", groups = {CreateUserFormGroup.class})
    private String password;

    @NotNull(message = "Role is required", groups = {CreateUserFormGroup.class})
    private UUID roleId;

    private Date createAt;
}
