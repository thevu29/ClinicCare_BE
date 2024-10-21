package com.example.cliniccare.controller;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.MedicalRecordGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.MedicalRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/medical-records")
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

    @GetMapping
    public ResponseEntity<?> getMedicalRecords() {
        try {
            List<MedicalRecordDTO> medicalRecords = medicalRecordService.getMedicalRecord();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get all medical records successfully", medicalRecords
            ));
        } catch (Exception e) {
            logger.error("Failed to get all medical records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get all medical records", null
            ));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getMedicalRecordsByPatientId(@PathVariable UUID patientId) {
        try {
            List<MedicalRecordDTO> medicalRecords = medicalRecordService.getMedicalRecordByPatientId(patientId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get all medical records by patient id successfully", medicalRecords
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get all medical records by patient id: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get all medical records by patient id", null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable UUID id) {
        try {
            MedicalRecordDTO user = medicalRecordService.getMedicalRecordById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get medical record successfully", user
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get medical record", null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createMedicalRecord(
            @Validated(MedicalRecordGroup.Create.class) @RequestBody MedicalRecordDTO medicalRecordDTO,
            BindingResult bindingResult
    ) {
        try {
            if (handleValidate(bindingResult) != null) {
                return handleValidate(bindingResult);
            }

            MedicalRecordDTO medicalRecord = medicalRecordService.createMedicalRecord(medicalRecordDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Create medical record successfully", medicalRecord
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to create medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable UUID id,
            @Validated(MedicalRecordGroup.Update.class) @RequestBody MedicalRecordDTO medicalRecordDTO,
            BindingResult bindingResult
    ) {
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
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to update medical record: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
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
