package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.PromotionFormGroup;
import com.example.cliniccare.model.Promotion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PromotionDTO {
    private UUID promotionId;

    @NotNull(message = "Discount is required", groups = {PromotionFormGroup.Create.class})
    @Min(value = 1, message = "Discount must be greater than 0")
    private Integer discount;

    private String description;

    @NotBlank(message = "Status is required", groups = {PromotionFormGroup.Create.class})
    private String status;

    private LocalDateTime createAt;

    @NotNull(message = "Expired At is required", groups = {PromotionFormGroup.Create.class})
    @Future(message = "Expiration date must be in the future")
    private LocalDate expireAt;

    public PromotionDTO() {}

    public PromotionDTO(Promotion promotion) {
        this.promotionId = promotion.getPromotionId();
        this.discount = promotion.getDiscount();
        this.description = promotion.getDescription();
        this.createAt = promotion.getCreateAt();
        this.expireAt = promotion.getExpireAt();
        this.status = String.valueOf(promotion.getStatus());
    }
}
