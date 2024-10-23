package com.example.cliniccare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private String message;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        isRead = false;
    }
}
