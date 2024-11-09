package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Promotion;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PaginationService paginationService;
    private final UserRepository userRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository, PaginationService paginationService, UserRepository userRepository) {
        this.promotionRepository = promotionRepository;
        this.paginationService = paginationService;
        this.userRepository = userRepository;
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

    public PaginationResponse<List<PromotionDTO>> getPromotions(PaginationDTO paginationQuery, String search) {
        Pageable pageable = paginationService.getPageable(paginationQuery);

        Page<Promotion> promotions = search.isEmpty() ? promotionRepository.findBy(pageable) : promotionRepository.findByDiscountContaining(search, pageable);

        int totalPage = paginationService.getTotalPages(promotions.getTotalElements(),paginationQuery.size);
        long totalElements = promotions.getTotalElements();

        return new PaginationResponse<>(
                true,
                "Get users successfully",
                promotions.map(PromotionDTO::new).getContent(),
                paginationQuery.page,
                paginationQuery.size,
                totalPage,
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

    public PromotionDTO updatePromotion(PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findByPromotionId(promotionDTO.getPromotionId())
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
