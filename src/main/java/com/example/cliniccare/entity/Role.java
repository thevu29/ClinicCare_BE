package com.example.cliniccare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    private UUID roleId;

    private String name;

    private String description;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    private List<User> userList;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
