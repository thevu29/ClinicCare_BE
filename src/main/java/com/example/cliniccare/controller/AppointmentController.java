package com.example.cliniccare.controller;

import com.example.cliniccare.dto.AppointmentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.AppointmentGroup;
import com.example.cliniccare.response.ApiResponse;
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

    @GetMapping
    public ResponseEntity<?> getAppointments() {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAppointments();

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get appointments successfully", appointments
            ));
        } catch (Exception e) {
            logger.error("Failed to get appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable UUID patientId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getPatientAppointments(patientId);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get patient appointments successfully", appointments
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get patient appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable UUID doctorId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getDoctorAppointments(doctorId);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get doctor appointments successfully", appointments
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get doctor appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
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
                    false, e.getMessage(), null
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
                    false, e.getMessage(), null
            ));
        }
    }
}
