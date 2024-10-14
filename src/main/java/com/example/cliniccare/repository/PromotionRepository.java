package com.example.cliniccare.repository;

import com.example.cliniccare.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByDeleteAtIsNull();
    Optional<Promotion> findByPromotionIdAndDeleteAtIsNull(UUID promotionId);
}
