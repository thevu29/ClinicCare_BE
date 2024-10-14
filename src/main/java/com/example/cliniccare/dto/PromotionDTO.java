package com.example.cliniccare.dto;

import com.example.cliniccare.model.Promotion;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.UUID;


@Data
public class PromotionDTO {

    private UUID promotionId;

    @NotBlank(message = "Description is required")
    private String description;

    private int discount;


    public PromotionDTO() {}

    public PromotionDTO(Promotion promotion) {
        this.promotionId = promotion.getPromotionId();
        this.description = promotion.getDescription();
        this.discount = promotion.getDiscount();
    }
}