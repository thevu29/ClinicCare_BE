package com.example.cliniccare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPromotionDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount is required")
    private int discount;
}