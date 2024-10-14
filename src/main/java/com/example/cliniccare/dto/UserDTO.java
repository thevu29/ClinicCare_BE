package com.example.cliniccare.dto;

import com.example.cliniccare.model.User;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Date createAt;

    public UserDTO() {}

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole().getName();
        this.createAt = user.getCreateAt();
    }
}
