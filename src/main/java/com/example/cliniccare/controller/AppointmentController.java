package com.example.cliniccare.controller;

import com.example.cliniccare.dto.AppointmentDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.AppointmentGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.service.AppointmentService;
import com.example.cliniccare.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAppointments() {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAllAppointments();

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get all appointments successfully", appointments
            ));
        } catch (Exception e) {
            logger.error("Failed to get all appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get all appointments", null
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAppointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue =  "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID userId
    ) {
        try {
            PaginationDTO paginationDTO = new PaginationDTO(page, size, sortBy, order);
            PaginationResponse<List<AppointmentDTO>> response = appointmentService
                    .getAppointments(paginationDTO, search, date, status, patientId, userId);

            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, ex.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get appointments", null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get appointment successfully", appointment
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get appointment by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get appointment by ID", null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(
            @Validated(AppointmentGroup.Create.class) @RequestBody AppointmentDTO appointmentDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            AppointmentDTO newAppointment = appointmentService.createAppointment(appointmentDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create appointment successfully", newAppointment
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
            logger.error("Failed to create appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to create appointment", null
            ));
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable UUID id,
            @Validated(AppointmentGroup.Cancel.class) @RequestBody AppointmentDTO appointmentDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            AppointmentDTO appointment = appointmentService.cancelAppointment(id, appointmentDTO);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Cancel appointment successfully", appointment
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
            logger.error("Failed to cancel appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to cancel appointment", null
            ));
        }
    }

    @GetMapping("/appointment-statistics")
    public ResponseEntity<?> getAppointmentStatistics(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        try {
            if (month == null) {
                month = LocalDateTime.now().getMonthValue();
            }
            if (year == null) {
                year = LocalDateTime.now().getYear();
            }

            long count = appointmentService.getAppointmentCountForMonth(month, year);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get appointment statistics successfully", count
            ));
        } catch (Exception e) {
            logger.error("Failed to retrieve appointment statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to get appointment statistics", null
            ));
        }
    }
}
