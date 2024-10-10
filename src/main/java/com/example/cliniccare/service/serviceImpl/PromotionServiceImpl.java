package com.example.cliniccare.service.serviceImpl;

import com.example.cliniccare.model.Promotion;
import com.example.cliniccare.repository.PromotionRepository;
import com.example.cliniccare.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    PromotionRepository promotionRepository;

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Promotion readPromotionById(UUID id) throws Exception {
        return promotionRepository.findById(id).orElseThrow(() -> new Exception("Promotion not found"));
    }

    @Override
    public Promotion updatePromotion(Promotion promotion) throws Exception {
        return promotionRepository.save(promotion);
    }

    @Override
    public void deletePromotion(UUID id) throws Exception {
        promotionRepository.deleteById(id);
    }
}
