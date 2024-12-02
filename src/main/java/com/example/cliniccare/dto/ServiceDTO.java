package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.ServiceFormGroup;
import com.example.cliniccare.entity.Service;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ServiceDTO {
    private UUID serviceId;

    @NotBlank(message = "Name is required", groups = {ServiceFormGroup.Create.class})
    private String name;

    private String description;

    @NotNull(message = "Price is required", groups = {ServiceFormGroup.Create.class})
    @Min(value = 1, message = "Price must be greater than 0", groups = {ServiceFormGroup.Create.class, ServiceFormGroup.Update.class})
    private Double price;

    private MultipartFile imageFile;
    private String image;

    private LocalDateTime createAt;

    private String status;

    @NotNull(message = "Promotion is required", groups = {ServiceFormGroup.ApplyPromotion.class})
    private UUID promotionId;

    private int promotionDiscount;

    public ServiceDTO(Service service) {
        this.serviceId = service.getServiceId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.image = service.getImage();
        this.status = service.getStatus().name();
        this.createAt = service.getCreateAt();
        this.promotionId = service.getPromotion() == null ? null : service.getPromotion().getPromotionId();
        this.promotionDiscount = service.getPromotion() == null ? 0 : service.getPromotion().getDiscount();
    }
}
