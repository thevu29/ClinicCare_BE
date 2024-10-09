package com.example.cliniccare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {

    private UUID promotionId;


    private String description;

    @NotBlank(message = "Discount is required")
    private int discount;

    private Date createAt;

    private String deleteAt;

}