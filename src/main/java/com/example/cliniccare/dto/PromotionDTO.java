package com.example.cliniccare.dto;

import com.example.cliniccare.model.Promotion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class PromotionDTO {
    private UUID promotionId;

    @NotBlank(message = "Discount is required")
    @Pattern(regexp = "^[0-9]*$", message = "Discount must be a number")
    @Min(value = 0, message = "Discount must be greater than 0")
    private int discount;

    private String description;

    private Date createdAt;

    @NotNull(message = "Expired at is required")
    private Date expiredAt;

    @NotNull(message = "Status is required")
    private Promotion.PromotionStatus status;

    public PromotionDTO() {}

    public PromotionDTO(Promotion promotion) {
        this.promotionId = promotion.getPromotionId();
        this.description = promotion.getDescription();
        this.discount = promotion.getDiscount();
    }
}
