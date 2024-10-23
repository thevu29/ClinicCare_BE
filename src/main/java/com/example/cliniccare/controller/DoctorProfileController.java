package com.example.cliniccare.controller;

import com.example.cliniccare.dto.DoctorProfileDTO;
import com.example.cliniccare.dto.DoctorProfileFormDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.DoctorProfileGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.service.DoctorProfileService;
import com.example.cliniccare.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/doctors")
public class DoctorProfileController {
    private static final Logger logger = LoggerFactory.getLogger(DoctorProfileController.class);
    private final DoctorProfileService doctorProfileService;

    @Autowired
    public DoctorProfileController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping
    public ResponseEntity<?> getDoctorProfiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "doctorProfileId") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "") String search) {
        try {
            PaginationDTO paginationQuery = new PaginationDTO(page, size, sortBy, order);
            PaginationResponse<List<DoctorProfileDTO>> response = doctorProfileService.getDoctorProfiles(paginationQuery, search);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get doctors: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorProfileById(@PathVariable UUID id) {
        try {
            DoctorProfileDTO doctorProfile = doctorProfileService.getDoctorProfileById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get doctor successfully", doctorProfile
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get doctor: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createDoctorProfile(
            @Validated(DoctorProfileGroup.Create.class) @ModelAttribute DoctorProfileFormDTO doctorProfileDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            DoctorProfileDTO doctorProfile = doctorProfileService.createDoctorProfile(doctorProfileDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create doctor successfully", doctorProfile
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (IOException e) {
            logger.error("Failed to upload avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to upload avatar", null
            ));
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to create doctorProfile", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDoctorProfile(
            @PathVariable UUID id,
            @Validated(DoctorProfileGroup.Update.class) @ModelAttribute DoctorProfileFormDTO doctorProfileDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            DoctorProfileDTO doctorProfile = doctorProfileService.updateDoctorProfile(id, doctorProfileDTO);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update doctor profile successfully", doctorProfile
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (IOException e) {
            logger.error("Failed to upload avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to upload avatar", null
            ));
        } catch (Exception e) {
            logger.error("Failed to update doctorProfile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to update doctorProfile", null
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDoctorProfile(@PathVariable UUID id) {
        try {
            doctorProfileService.deleteDoctorProfile(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete doctor successfully", null
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to delete doctor profile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }
}