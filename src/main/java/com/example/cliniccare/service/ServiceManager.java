package com.example.cliniccare.service;

import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceManager {
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;

    @Autowired
    public ServiceManager(ServiceRepository serviceRepository, PromotionRepository promotionRepository) {
        this.serviceRepository = serviceRepository;
        this.promotionRepository = promotionRepository;
    }

    public List<ServiceDTO> getAllServices() {
        List<Service> services = serviceRepository.findAllByDeleteAtIsNull();
        return services.stream().map(ServiceDTO::new).toList();
    }

    public ServiceDTO getServiceById(UUID id) {
        Service service = serviceRepository.findByServiceIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        return new ServiceDTO(service);
    }

    public ServiceDTO createService(ServiceDTO serviceDTO) {
        Service service = new Service();
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
        service.setPromotion(promotionRepository.findByPromotionId(serviceDTO.getPromotionId()).orElse(null));

        if (serviceDTO.getStatus() == null || serviceDTO.getStatus().isEmpty()) {
            service.setStatus(Service.ServiceStatus.AVAILABLE);
        } else {
            service.setStatus(Service.ServiceStatus.valueOf(serviceDTO.getStatus().toUpperCase()));
        }

        Service savedService = serviceRepository.save(service);
        return new ServiceDTO(savedService);
    }

    public ServiceDTO updateService(UUID id, ServiceDTO serviceDTO) {
        Service service = serviceRepository.findByServiceIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        service.setDescription(serviceDTO.getDescription());

        if (serviceDTO.getName() != null && !serviceDTO.getName().isEmpty()) {
            service.setName(serviceDTO.getName());
        }
        if (serviceDTO.getPrice() != null) {
            service.setPrice(serviceDTO.getPrice());
        }
        if (serviceDTO.getPromotionId() != null) {
            service.setPromotion(promotionRepository.findByPromotionId(serviceDTO.getPromotionId())
                    .orElseThrow(() -> new NotFoundException("Promotion not found")));
        }
        if (serviceDTO.getStatus() != null && !serviceDTO.getStatus().isEmpty()) {
            service.setStatus(Service.ServiceStatus.valueOf(serviceDTO.getStatus().toUpperCase()));
        }

        Service savedService = serviceRepository.save(service);
        return new ServiceDTO(savedService);
    }

    public ServiceDTO applyPromotion(UUID serviceId, UUID promotionId) {
        Service service = serviceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        service.setPromotion(promotionRepository.findByPromotionId(promotionId)
                .orElseThrow(() -> new NotFoundException("Promotion not found")));

        Service savedService = serviceRepository.save(service);
        return new ServiceDTO(savedService);
    }

    public ServiceDTO deleteService(UUID id) {
        Service service = serviceRepository.findByServiceIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        service.setStatus(Service.ServiceStatus.UNAVAILABLE);
        service.setDeleteAt(new Date());
        serviceRepository.save(service);
        return new ServiceDTO(service);
    }
}