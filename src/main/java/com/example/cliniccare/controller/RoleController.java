package com.example.cliniccare.controller;

import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/role")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/all")
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
}
