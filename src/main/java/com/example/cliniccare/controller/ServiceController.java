package com.example.cliniccare.controller;

import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.ServiceManager;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    @Autowired
    private final ServiceManager serviceManager;
    @Autowired
    private ServiceRepository serviceRepository;

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

    @GetMapping("/all")
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
        }
        catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, e.getMessage(), null));
        }

        catch (Exception e) {
            logger.error("Failed to get service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get service", null
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceDTO serviceDTO, BindingResult bindingResult) {
        try {
            if(handleValidate(bindingResult) != null) return handleValidate(bindingResult);

            ServiceDTO service = serviceManager.createService(serviceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Create service successfully", service
            ));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (Exception e) {
            logger.error("Failed to create service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to create service", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateService(@PathVariable UUID id, @RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceDTO service = serviceManager.updateService(id,serviceDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update service successfully", service
            ));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (Exception e) {
            logger.error("Failed to update service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to update service", null
            ));
        }
    }

    @PutMapping("/apply-promotion")
    public ResponseEntity<?> applyPromotion(
            @RequestBody ServiceDTO applyPromotion // Request body only get type of value is object (serviceId, promotionId)
            // So I knew my code like a dick, Please fix it for me (love per you)
    ) {
        try {
            ServiceDTO service = serviceManager.applyPromotion(applyPromotion.getServiceId(), applyPromotion.getPromotionId());
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Apply promotion successfully", service
            ));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (Exception e) {
            logger.error("Failed to apply promotion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to apply promotion", null
            ));
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteService(@PathVariable UUID id) {
        try {
            ServiceDTO service = serviceManager.deleteService(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete service successfully", service
            ));
        }
        catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
        catch (Exception e) {
            logger.error("Failed to delete service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to delete service", null
            ));
        }
    }
}
