package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.dto.TopServiceDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.Service;
import com.example.cliniccare.repository.PaymentRepository;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.NumberQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceManager {
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final PaginationService paginationService;
    private final FirebaseStorageService firebaseStorageService;

    @Autowired
    public ServiceManager(
            ServiceRepository serviceRepository,
            PromotionRepository promotionRepository,
            PaymentRepository paymentRepository,
            PaginationService paginationService,
            FirebaseStorageService firebaseStorageService
    ) {
        this.serviceRepository = serviceRepository;
        this.promotionRepository = promotionRepository;
        this.paymentRepository = paymentRepository;
        this.paginationService = paginationService;
        this.firebaseStorageService = firebaseStorageService;
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

    public List<ServiceDTO> getAllServices(String search) {
        List<Service> services = search != null && !search.trim().isEmpty()
                ? serviceRepository.findAllByDeleteAtIsNullAndNameContaining(search)
                : serviceRepository.findAllByDeleteAtIsNull();

        return services.stream().map(ServiceDTO::new).toList();
    }

    public PaginationResponse<List<ServiceDTO>> getServices(
            PaginationDTO paginationDTO,
            String search,
            String price,
            String status
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Service> spec = Specification
                .where((root, query, cb) -> cb.isNull(root.get("deleteAt")));

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("name"), "%" + search + "%"));
        }
        if (price != null && !price.isEmpty()) {
            NumberQueryParser<Service> numberQueryParser = new NumberQueryParser<>(price, "price");
            spec = spec.and(numberQueryParser.createPriceSpecification());
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), getServiceStatus(status)));
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

    public ServiceDTO createService(ServiceDTO serviceDTO) throws IOException {
        if (serviceDTO.getImageFile() == null || serviceDTO.getImageFile().isEmpty()) {
            throw new BadRequestException("Image is required");
        }

        Service service = new Service();
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
        service.setImage(firebaseStorageService.uploadImage(serviceDTO.getImageFile()));
        service.setStatus(getServiceStatus(serviceDTO.getStatus()));
        service.setPromotion(promotionRepository.findByPromotionId(serviceDTO.getPromotionId()).orElse(null));

        Service savedService = serviceRepository.save(service);
        return new ServiceDTO(savedService);
    }

    public ServiceDTO updateService(UUID id, ServiceDTO serviceDTO) throws IOException {
        Service service = serviceRepository.findByServiceIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        service.setDescription(serviceDTO.getDescription());

        if (serviceDTO.getName() != null && !serviceDTO.getName().isEmpty()) {
            service.setName(serviceDTO.getName());
        }
        if (serviceDTO.getPrice() != null) {
            service.setPrice(serviceDTO.getPrice());
        }
        if (serviceDTO.getImageFile() != null && !serviceDTO.getImageFile().isEmpty()) {
            service.setImage(firebaseStorageService.updateImage(serviceDTO.getImageFile(), service.getImage()));
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

    public List<TopServiceDTO> getTopServices(Integer top) {
        if (top == null || top <= 0) {
            throw new BadRequestException("Top must be greater than 0");
        }

        Pageable pageable = PageRequest.of(0, top);
        List<Object[]> topServices = paymentRepository.findTopServices(pageable);
        List<TopServiceDTO> serviceDTOs = new ArrayList<>();

        for (Object[] row : topServices) {
            UUID serviceId = (UUID) row[0];
            long usageCount = ((Number) row[1]).longValue();

            Service service = serviceRepository.findByServiceId(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            TopServiceDTO serviceDTO = new TopServiceDTO(service, usageCount);

            serviceDTOs.add(serviceDTO);
        }

        return serviceDTOs;
    }
}
