package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.PriceQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceManager {
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final PaginationService paginationService;

    @Autowired
    public ServiceManager(
            ServiceRepository serviceRepository,
            PromotionRepository promotionRepository,
            PaginationService paginationService
    ) {
        this.serviceRepository = serviceRepository;
        this.promotionRepository = promotionRepository;
        this.paginationService = paginationService;
    }

    private Service.ServiceStatus getServiceStatus(String status) {
        try {
            return status != null && !status.isEmpty()
                    ? Service.ServiceStatus.valueOf(status.toUpperCase())
                    : Service.ServiceStatus.AVAILABLE;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Service status not found");
        }
    }


    public PaginationResponse<List<ServiceDTO>> getServices(
            PaginationDTO paginationDTO, String search, String price, String status
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Service> spec = Specification.where((root, query, cb) -> cb.isNull(root.get("deleteAt")));

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("name"), "%" + search + "%"));
        }
        if (price != null && !price.isEmpty()) {
            PriceQueryParser<Service> priceQueryParser = new PriceQueryParser<>(price, "price");
            spec = spec.and(priceQueryParser.createPriceSpecification());
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), getServiceStatus(status)));
        }

        Page<Service> services = serviceRepository.findAll(spec, pageable);

        int totalPages = services.getTotalPages();
        long totalElements = services.getTotalElements();
        int take = services.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get services successfully",
                services.getContent().stream().map(ServiceDTO::new).toList(),
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
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
        service.setStatus(getServiceStatus(serviceDTO.getStatus()));
        service.setPromotion(promotionRepository.findByPromotionId(serviceDTO.getPromotionId()).orElse(null));

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
            service.setStatus(getServiceStatus(serviceDTO.getStatus()));
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

    public ServiceDTO removePromotion(UUID serviceId) {
        Service service = serviceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        if (service.getPromotion() == null) {
            throw new BadRequestException("Service does not have promotion");
        }

        service.setPromotion(null);

        Service savedService = serviceRepository.save(service);
        return new ServiceDTO(savedService);
    }

    public ServiceDTO deleteService(UUID id) {
        Service service = serviceRepository.findByServiceIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        service.setStatus(Service.ServiceStatus.UNAVAILABLE);
        service.setDeleteAt(LocalDateTime.now());
        serviceRepository.save(service);
        return new ServiceDTO(service);
    }
}
