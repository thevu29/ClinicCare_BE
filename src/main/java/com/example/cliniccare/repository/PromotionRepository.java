package com.example.cliniccare.repository;

import com.example.cliniccare.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByPromotionId(UUID promotionId);
    Page<Promotion> findBy(Pageable pageable);
    Page<Promotion> findByDiscountContaining(String discount, Pageable pageable);

}
