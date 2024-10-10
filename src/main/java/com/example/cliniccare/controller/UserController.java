package com.example.cliniccare.controller;

import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.exception.ResourceNotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.UserService;
import com.example.cliniccare.utils.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getUsers() {
        try {
            List<UserDTO> users = userService.getUsers();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get users successfully", users
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get user successfully", user
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserFormDTO userDTO,
            BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) {
                String errors = bindingResult.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        false, errors, null
                ));
            }

            if (!userDTO.getPhone().isEmpty() && !Validation.isPhoneValid(userDTO.getPhone())) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        false, "Invalid phone number format", null
                ));
            }

            UserDTO user = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create user successfully", user
            ));
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to create user", null
            ));
        }
    }
}