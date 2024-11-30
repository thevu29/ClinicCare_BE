package com.example.cliniccare.entity;

import com.example.cliniccare.interfaces.RegistrationProcessRequestGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationProcessRequest {
    @NotBlank(
            message = "Email is required",
            groups = {RegistrationProcessRequestGroup.SendOtp.class, RegistrationProcessRequestGroup.VerifyOtp.class}
    )
    @Pattern(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is required", groups = {RegistrationProcessRequestGroup.VerifyOtp.class})
    @Pattern(regexp = "^\\d{6}$", message = "Invalid OTP format")
    private String otp;
}
