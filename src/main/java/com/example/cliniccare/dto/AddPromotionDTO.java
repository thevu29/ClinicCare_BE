package com.example.cliniccare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;


@Data
public class AddPromotionDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount is required")
    private int discount;

}