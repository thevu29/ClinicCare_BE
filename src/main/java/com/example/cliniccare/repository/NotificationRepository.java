package com.example.cliniccare.repository;

import com.example.cliniccare.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByNotificationIdAndDeleteAtIsNull(UUID notificationId);
}
