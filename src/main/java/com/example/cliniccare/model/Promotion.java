package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_id")
    private UUID promotionId;

    private String description;

    private int discount;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "delete_at")
    private String deleteAt;

    @OneToMany(mappedBy = "promotion")
    private List<Service> serviceList;
}
