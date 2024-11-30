package com.example.cliniccare.controller;

import com.example.cliniccare.dto.LoginDTO;
import com.example.cliniccare.entity.AuthRequest;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.AuthService;
import com.example.cliniccare.service.JwtService;
import com.example.cliniccare.entity.UserInfoDetails;
import com.example.cliniccare.service.TokenBlacklistService;
import com.example.cliniccare.validation.Validation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            AuthService authService,
            AuthenticationManager authenticationManager
    ) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest, BindingResult bindingResult) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserInfoDetails userDetails = (UserInfoDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Login successfully",
                    new LoginDTO(userDetails, jwt, refreshToken)
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid username or password", null));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to login", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Token is missing", null));
        }

        String token = authHeader.substring("Bearer ".length());
        tokenBlacklistService.blacklistToken(token);

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse<>(true, "Logout successfully", null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            UserInfoDetails userDetails = (UserInfoDetails) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, null)
            ).getPrincipal();

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                String newToken = jwtService.generateToken(userDetails);
                return ResponseEntity.ok(new ApiResponse<>(
                        true,
                        "Token refreshed successfully",
                        newToken)
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid refresh token", null));
            }
        } catch (Exception e) {
            logger.error("Failed to login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to refresh token", null));
        }
    }

    @GetMapping("/oauth2-data")
    public ResponseEntity<?> getOAuth2Data(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized", null));
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        User user = authService.findOrCreateUserFromOAuth(email, name);
        UserInfoDetails userDetails = new UserInfoDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Login successfully",
                new LoginDTO(userDetails, accessToken, refreshToken)
        ));
    }
}
