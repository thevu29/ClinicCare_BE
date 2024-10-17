package com.example.cliniccare.dto;

import com.example.cliniccare.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class RoleDTO {
    private UUID roleId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Date createAt;

    public RoleDTO() {}

    public RoleDTO(Role role) {
        this.roleId = role.getRoleId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.createAt = role.getCreateAt();
    }
}
