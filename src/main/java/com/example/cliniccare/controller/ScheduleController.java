package com.example.cliniccare.controller;

import com.example.cliniccare.dto.ScheduleDTO;
import com.example.cliniccare.dto.ScheduleFormDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.ScheduleFormGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.ScheduleService;
import com.example.cliniccare.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("api/schedules")
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<?> getSchedules() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get schedules successfully", scheduleService.getSchedules()
            ));
        } catch (Exception e) {
            logger.error("Failed to get schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/doctor/{doctorProfileId}")
    public ResponseEntity<?> getDoctorSchedules(@PathVariable UUID doctorProfileId) {
        try {
            List<ScheduleDTO> doctorSchedules = scheduleService.getDoctorSchedules(doctorProfileId);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get doctor schedules successfully", doctorSchedules
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get doctor schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getServiceSchedules(@PathVariable UUID serviceId) {
        try {
            List<ScheduleDTO> serviceSchedules = scheduleService.getServiceSchedules(serviceId);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    true, "Get service schedules successfully", serviceSchedules
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to get service schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createSchedule(
            @Validated(ScheduleFormGroup.Create.class) @RequestBody ScheduleFormDTO scheduleDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            ScheduleDTO newSchedule = scheduleService.createSchedule(scheduleDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create schedule successfully", newSchedule
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
            logger.error("Failed to create schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleFormDTO scheduleDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleDTO);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                true, "Update schedule successfully", updatedSchedule
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
            logger.error("Failed to update schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                false, e.getMessage(), null
            ));
        }
    }
}
