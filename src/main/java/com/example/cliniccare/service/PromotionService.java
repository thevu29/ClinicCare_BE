package com.example.cliniccare.service;

import com.example.cliniccare.model.Promotion;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public interface PromotionService {
    List<Promotion> getAllPromotions();
    Promotion readPromotionById(UUID id) throws Exception;
    Promotion updatePromotion(Promotion promotion) throws Exception;
    void deletePromotion(String id) throws Exception;
}
