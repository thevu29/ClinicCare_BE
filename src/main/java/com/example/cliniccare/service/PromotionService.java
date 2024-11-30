package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.Promotion;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.NumberQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PaginationService paginationService;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository, PaginationService paginationService) {
        this.promotionRepository = promotionRepository;
        this.paginationService = paginationService;
    }

    private Promotion.PromotionStatus getPromotionStatus(String status) {
        try {
            return status != null && !status.isEmpty()
                    ? Promotion.PromotionStatus.valueOf(status.toUpperCase())
                    : Promotion.PromotionStatus.ACTIVE;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid promotion status");
        }
    }

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream().map(PromotionDTO::new).toList();
    }

    public PaginationResponse<List<PromotionDTO>> getPromotions(
            PaginationDTO paginationDTO,
            String search,
            String status,
            String discount
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Promotion> spec = Specification.where(null);

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), getPromotionStatus(status)));
        }
        if (discount != null && !discount.isEmpty()) {
            NumberQueryParser<Promotion> numberQueryParser = new NumberQueryParser<>(discount, "discount");
            spec = spec.and(numberQueryParser.createPriceSpecification());
        }

        Page<Promotion> promotions = promotionRepository.findAll(spec, pageable);

        int totalPages = promotions.getTotalPages();
        long totalElements = promotions.getTotalElements();
        int take = promotions.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get promotions successfully",
                promotions.getContent().stream().map(PromotionDTO::new).toList(),
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
    }

    public PromotionDTO getPromotionById(UUID id) {
        Promotion promotion = promotionRepository.findByPromotionId(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found"));
        return new PromotionDTO(promotion);
    }

    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        Promotion promotion = new Promotion();
        promotion.setDiscount(promotionDTO.getDiscount());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setStatus(getPromotionStatus(promotionDTO.getStatus()));
        promotion.setExpireAt(promotionDTO.getExpireAt());

        Promotion savedPromotion = promotionRepository.save(promotion);
        return new PromotionDTO(savedPromotion);
    }

    public PromotionDTO updatePromotion(UUID id, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findByPromotionId(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        if (promotionDTO.getDiscount() != null) {
            promotion.setDiscount(promotionDTO.getDiscount());
        }
        if (promotionDTO.getExpireAt() != null && !promotionDTO.getExpireAt().toString().isEmpty()) {
            promotion.setExpireAt(promotionDTO.getExpireAt());
        }
        if (promotionDTO.getStatus() != null && !promotionDTO.getStatus().isEmpty()) {
            promotion.setStatus(getPromotionStatus(promotionDTO.getStatus()));
        }
        promotion.setDescription(promotionDTO.getDescription());

        Promotion savedPromotion = promotionRepository.save(promotion);
        return new PromotionDTO(savedPromotion);
    }
}
