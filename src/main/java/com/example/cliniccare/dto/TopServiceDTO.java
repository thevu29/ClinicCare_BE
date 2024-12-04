package com.example.cliniccare.dto;

import com.example.cliniccare.entity.Service;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class TopServiceDTO {
    private UUID serviceId;
    private String name;
    private String image;
    private long count;

    public TopServiceDTO(Service service, long count) {
        this.serviceId = service.getServiceId();
        this.name = service.getName();
        this.image = service.getImage();
        this.count = count;
    }
}
