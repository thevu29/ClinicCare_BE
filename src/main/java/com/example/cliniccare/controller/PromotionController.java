package com.example.cliniccare.controller;

import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.PromotionFormGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.PromotionService;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.groups.Default;
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
@RequestMapping("api/promotion")
public class PromotionController {
    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getPromotions() {
        try{
            List<PromotionDTO> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get promotions successfully", promotions
            ));
        } catch (Exception e) {
            logger.error("Failed to get promotions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get promotions", null
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPromotion(
            @Validated({Default.class, PromotionFormGroup.Create.class}) @RequestBody PromotionDTO promotionDTO,
            BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) {
                String errors = bindingResult.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new ApiResponse<>(false, errors, null)
                );
            }

            PromotionDTO promotion = promotionService.createPromotion(promotionDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Create promotion successfully", promotion
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
                    false, "Status must be one of the following: Active, Inactive, Expired, or End", null
            ));
        } catch (Exception e) {
            logger.error("Failed to create promotion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to create promotion", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePromotion(
            @PathVariable UUID id,
            @Validated({Default.class, PromotionFormGroup.Update.class}) @RequestBody PromotionDTO promotionDTO
    ) {
        try {
            promotionDTO.setPromotionId(id);
            PromotionDTO promotion = promotionService.updatePromotion(promotionDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update promotion successfully", promotion
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
                    false, "Status must be one of the following: Active, Inactive, Expired, or End.", null
            ));
        } catch (Exception e) {
            logger.error("Failed to update promotion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to update promotion", null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID id) {
        try {
            PromotionDTO promotion = promotionService.getPromotionById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get promotion successfully", promotion
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }
}
