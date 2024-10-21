package com.example.cliniccare.controller;

import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.RoleService;
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
@RequestMapping("/api/roles")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    public ResponseEntity<?> handleValidate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, errors, null
            ));
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getRoles() {
        try {
            List<RoleDTO> roles = roleService.getRoles();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get roles successfully", roles
            ));
        } catch (Exception e) {
            logger.error("Failed to get roles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable UUID id) {
        try {
            RoleDTO role = roleService.getRoleById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get role successfully", role
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get role: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get role", null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createRole(
            @Valid @RequestBody RoleDTO roleDTO,
            BindingResult bindingResult
    ) {
        try {
            if (handleValidate(bindingResult) != null) {
                return handleValidate(bindingResult);
            }

            RoleDTO role = roleService.createRole(roleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create role successfully", role
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to create role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to create role", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable UUID id,
            @RequestBody RoleDTO roleDTO)
    {
        try {
            RoleDTO role = roleService.updateRole(id, roleDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update role successfully", role
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to update role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to update role", null
            ));
        }
    }
}
