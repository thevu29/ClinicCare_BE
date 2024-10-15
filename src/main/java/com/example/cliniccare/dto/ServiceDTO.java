package com.example.cliniccare.dto;

import com.example.cliniccare.model.Service;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class ServiceDTO {
    private UUID serviceId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Price is required")
    @Pattern(regexp = "\\d+", message = "Price must be a number")
    @Min(value = 0, message = "Price must be greater than 0")
    private double price;

    private Date createAt;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Promotion is required")
    private UUID promotionId;

    public ServiceDTO() {}

    public ServiceDTO(Service service) {
        this.serviceId = service.getServiceId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.status = service.getStatus().name();
        this.createAt = service.getCreateAt();
        this.promotionId = service.getPromotion().getPromotionId();
    }
}