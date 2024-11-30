package com.example.cliniccare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "promotions")
public class Promotion {
    public enum PromotionStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED,
        END
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_id")
    private UUID promotionId;

    private String description;

    private int discount;

    private PromotionStatus status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "expired_at")
    private LocalDate expireAt;

    @OneToMany(mappedBy = "promotion")
    @ToString.Exclude
    private List<Service> serviceList;

    @PrePersist
    public void onCreate() {
        createAt = LocalDateTime.now();
    }
}
