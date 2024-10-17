package com.example.cliniccare.controller;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.MedicalRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@CrossOrigin("*")
@RestController
@RequestMapping("api/medicalRecord")
public class MedicalRecordController {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
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

    @GetMapping("/all")
    public ResponseEntity<?> getMedicalRecords() {
        try {
            List<MedicalRecordDTO> medicalRecords = medicalRecordService.getMedicalRecord();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get medical records successfully", medicalRecords
            ));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable UUID id) {
        try {
            MedicalRecordDTO user = medicalRecordService.getMedicalRecordById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get user successfully", user
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PostMapping ("/create")
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordDTO medicalRecordDTO,  BindingResult bindingResult) {
        try {

            if (handleValidate(bindingResult) != null) {
                return handleValidate(bindingResult);
            }

            MedicalRecordDTO medicalRecord = medicalRecordService.createMedicalRecord(medicalRecordDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Create medical record successfully", medicalRecord
            ));
        } catch (Exception e) {
            logger.error("Failed to create medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicalRecord(@PathVariable UUID id, @RequestBody MedicalRecordDTO medicalRecordDTO, BindingResult bindingResult) {
        try {
            if (handleValidate(bindingResult) != null) {
                return handleValidate(bindingResult);
            }

            MedicalRecordDTO medicalRecord = medicalRecordService.updateMedicalRecord(id, medicalRecordDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update medical record successfully", medicalRecord
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to update medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedicalRecord(@PathVariable UUID id) {
        try {
            medicalRecordService.deleteMedicalRecord(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete medical record successfully", null
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to delete medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }
}
