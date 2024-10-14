package com.example.cliniccare.service;

import com.example.cliniccare.dto.AddPromotionDTO;
import com.example.cliniccare.dto.PromotionDTO;
import com.example.cliniccare.exception.ResourceNotFoundException;
import com.example.cliniccare.model.Promotion;
import com.example.cliniccare.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PromotionService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PromotionRepository promotionRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<PromotionDTO> getAllPromotions() {
        try{
            List<Promotion> promotions = promotionRepository.findByDeleteAtIsNull();
            return promotions.stream().map(PromotionDTO::new).toList();
        }
        catch (Exception e) {
            logger.error("Failed to get promotions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get promotions", e);
        }
    }

    public PromotionDTO getPromotionById(UUID id){
        try{
            Promotion promotion = promotionRepository.findByPromotionIdAndDeleteAtIsNull(id).orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
            return new PromotionDTO(promotion);
        }
        catch (Exception e) {
            logger.error("Failed to get promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get promotion", e);
        }
    }

    public PromotionDTO createPromotion(AddPromotionDTO promotionDTO) throws Exception {
        try{
            Promotion promotion = new Promotion();
            promotion.setDescription(promotionDTO.getDescription());

            Promotion savedPromotion = promotionRepository.save(promotion);
            return new PromotionDTO(savedPromotion);
        }
        catch(Exception e){
            logger.error("Failed to update promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update promotion", e);
        }
    }

    public PromotionDTO updatePromotion(PromotionDTO promotionDTO) {
        try{
            Promotion promotion = promotionRepository.findByPromotionIdAndDeleteAtIsNull(promotionDTO.getPromotionId()).orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
            promotion.setDescription(promotionDTO.getDescription());

            Promotion savedPromotion = promotionRepository.save(promotion);
            return new PromotionDTO(savedPromotion);
        }
        catch(Exception e){
            logger.error("Failed to update promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update promotion", e);
        }
    }

    public void deletePromotion(UUID id){
        try{
            Promotion promotion = promotionRepository.findByPromotionIdAndDeleteAtIsNull(id).orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
            promotion.setDeleteAt(new Date());
            promotionRepository.save(promotion);
        }
        catch(Exception e){
            logger.error("Failed to delete promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete promotion", e);
        }
    }
}
