package com.example.cliniccare.repository;

import com.example.cliniccare.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query(value = "SELECT * FROM notifications " +
            "WHERE user_id = :userId " +
            "ORDER BY create_at DESC, notification_id DESC " +
            "LIMIT :size", nativeQuery = true)
    List<Notification> findByUserId(
            @Param("userId") UUID userId,
            @Param("size") int size
    );

    @Query(value = "SELECT * FROM notifications " +
            "WHERE user_id = :userId AND " +
            "(create_at = :createAt AND notification_id < :notificationId " +
            "OR create_at < :createAt) " +
            "ORDER BY create_at DESC, notification_id DESC " +
            "LIMIT :size", nativeQuery = true)
    List<Notification> findByUserIdWithCursor(
            @Param("userId") UUID userId,
            @Param("createAt") String createAt,
            @Param("notificationId") String notificationId,
            @Param("size") int size
    );
}
