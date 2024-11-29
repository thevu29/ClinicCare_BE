package com.example.cliniccare.dto;

import com.example.cliniccare.entity.UserInfoDetails;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class LoginDTO {
    private UUID userId;
    private String username;
    private String role;
    private String name;
    private String access_token;
    private String refresh_token;

    public LoginDTO(UserInfoDetails user, String accessToken, String refreshToken) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.role = user.getAuthorities().stream().findFirst().orElseThrow().getAuthority();
        this.name = user.getName();
        this.access_token = accessToken;
        this.refresh_token = refreshToken;
    }
}
