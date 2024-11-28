package com.example.cliniccare.dto;

import com.example.cliniccare.entity.UserInfoDetails;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginDTO {
    private String username;
    private String role;
    private String access_token;
    private String refresh_token;

    public LoginDTO(UserInfoDetails user, String accessToken, String refreshToken) {
        this.username = user.getUsername();
        this.role = user.getAuthorities().stream().findFirst().orElseThrow().getAuthority();
        this.access_token = accessToken;
        this.refresh_token = refreshToken;
    }
}
