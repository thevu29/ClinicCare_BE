package com.example.cliniccare.controller;

import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.ServiceFormGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    private final ServiceManager serviceManager;

    @Autowired
    public ServiceController(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
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
    public ResponseEntity<?> getServices() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get services successfully", serviceManager.getAllServices()
            ));
        } catch (Exception e) {
            logger.error("Failed to get services: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable UUID id) {
        try {
            ServiceDTO service = serviceManager.getServiceById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get service successfully", service
            ));
        } catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to get service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get service", null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createService(
            @Validated(ServiceFormGroup.Create.class) @RequestBody ServiceDTO serviceDTO,
            BindingResult bindingResult
    ) {
        try {
            if (handleValidate(bindingResult) != null) return handleValidate(bindingResult);

            ServiceDTO service = serviceManager.createService(serviceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create service successfully", service
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, "Status must be one of the following: Available, Unavailable", null
            ));
        } catch (Exception e) {
            logger.error("Failed to create service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to create service", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable UUID id,
            @Validated(ServiceFormGroup.Update.class) @RequestBody ServiceDTO serviceDTO,
            BindingResult bindingResult
    ) {
        if (handleValidate(bindingResult) != null) return handleValidate(bindingResult);

        try {
            ServiceDTO service = serviceManager.updateService(id,serviceDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update service successfully", service
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
            logger.error("Failed to update service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to update service", null
            ));
        }
    }

    @PutMapping("/apply-promotion/{id}")
    public ResponseEntity<?> applyPromotion(
            @PathVariable UUID id,
            @Validated(ServiceFormGroup.ApplyPromotion.class) @RequestBody ServiceDTO applyPromotion,
            BindingResult bindingResult
    ) {
        if (handleValidate(bindingResult) != null) return handleValidate(bindingResult);

        try {
            ServiceDTO service = serviceManager.applyPromotion(id, applyPromotion.getPromotionId());
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Apply promotion successfully", service
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
            logger.error("Failed to apply promotion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to apply promotion", null
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteService(@PathVariable UUID id) {
        try {
            ServiceDTO service = serviceManager.deleteService(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete service successfully", service
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to delete service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to delete service", null
            ));
        }
    }
}
