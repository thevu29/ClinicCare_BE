package com.example.cliniccare.controller;

import com.example.cliniccare.model.Promotion;
import com.example.cliniccare.service.serviceImpl.PromotionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("api/promotion")
public class PromotionController {

    @Autowired
    private PromotionServiceImpl promotionServiceImpl;

    @GetMapping("")
    public List<Promotion> getUsers() {

        return promotionServiceImpl.getAllPromotions();
    }

    @PostMapping("")
    @ExceptionHandler
    public Promotion createPromotion(@RequestBody Promotion promotion) throws Exception {
        return promotionServiceImpl.updatePromotion(promotion);
    }

    @PutMapping("/{id}")
    @ExceptionHandler
    public Promotion updatePromotion(@RequestBody Promotion promotion) throws Exception {
        if(promotionServiceImpl.readPromotionById(promotion.getPromotionId()) == null) {
            throw new Exception("Promotion not found");
        }
        return promotionServiceImpl.updatePromotion(promotion);
    }

    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable UUID id) throws Exception {
        return promotionServiceImpl.readPromotionById(id);
    }

    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable UUID id) throws Exception {
        promotionServiceImpl.deletePromotion(id);
    }
}
