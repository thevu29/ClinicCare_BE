package com.example.cliniccare.controller;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.MedicalRecordGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.service.MedicalRecordService;
import com.example.cliniccare.utils.ExcelGenerator;
import com.example.cliniccare.validation.Validation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("api/medical-records")
public class MedicalRecordController {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPromotions() {
        try {
            List<MedicalRecordDTO> medicalRecords = medicalRecordService.getAllMedicalRecord();
            return ResponseEntity.ok(new ApiResponse<>(true, "Get all medical record successfully", medicalRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMedicalRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String date,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID serviceId
    ) {
        try {
            PaginationDTO paginationDTO = new PaginationDTO(page, size, sortBy, order);
            PaginationResponse<List<MedicalRecordDTO>> response = medicalRecordService
                    .getMedicalRecord(paginationDTO, search, date, patientId, doctorId, serviceId);

            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get all medical records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get all medical records", null
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

    @GetMapping("/export")
    public void exportMedicalRecords(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Access-Control-Expose-Headers", "content-disposition");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(System.currentTimeMillis());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=medical_records_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<MedicalRecordDTO> medicalRecordList = medicalRecordService.getAllMedicalRecord();
        ExcelGenerator excelGenerator = new ExcelGenerator(medicalRecordList);

        ByteArrayInputStream bis = excelGenerator.generateExcelFile();
        FileCopyUtils.copy(bis, response.getOutputStream());
    }


    @PostMapping
    public ResponseEntity<?> createMedicalRecord(
            @Validated(MedicalRecordGroup.Create.class) @RequestBody MedicalRecordDTO medicalRecordDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
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
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
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
