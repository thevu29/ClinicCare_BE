package com.example.cliniccare.controller;

import com.example.cliniccare.dto.AddPromotionDTO;
import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.PromotionService;
import jakarta.validation.Valid;
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

@CrossOrigin("*")
@RestController
@RequestMapping("api/promotion")
public class PromotionController {
    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("")
    public ResponseEntity<?> getPromotions() {
        try{
            List<PromotionDTO> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get promotions successfully", promotions
            ));
        }
        catch (Exception e) {
            logger.error("Failed to get promotions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to get promotions", null
            ));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createPromotion(@Valid @RequestBody AddPromotionDTO promotionDTO, BindingResult bindingResult) {
        try{
            if(bindingResult.hasErrors()) {
                String errors = bindingResult.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", "));
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, errors, null));
            }
            PromotionDTO promotion = promotionService.createPromotion(promotionDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Create promotion successfully", promotion
            ));
        }
        catch (Exception e) {
            logger.error("Failed to create promotion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, "Failed to create promotion", null
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable UUID id, @RequestBody PromotionDTO promotionDTO) {
        try{
            if(promotionService.getPromotionById(id) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                        false, "Promotion not found", null
                ));
            }
            PromotionDTO promotion = promotionService.updatePromotion(promotionDTO);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update promotion successfully", promotion
            ));
        }
        catch (Exception e) {
            logger.error("Failed to update promotion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to update promotion", null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID id) {
        try{
            PromotionDTO promotion = promotionService.getPromotionById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get promotion successfully", promotion
            ));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID id) {
        try{
            if(promotionService.getPromotionById(id) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                        false, "Promotion not found", null
                ));
            }
            promotionService.deletePromotion(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete promotion successfully", null
            ));
        }
        catch(Exception e){
            logger.error("Failed to delete promotion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to delete promotion", null
            ));
        }
    }
}
