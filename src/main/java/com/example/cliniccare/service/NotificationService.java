package com.example.cliniccare.service;

import com.example.cliniccare.dto.NotificationDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Notification;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.NotificationRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getNotifications(String cursor, UUID userId, int size) {
        List<Notification> notifications;

        if (cursor.isEmpty()) {
            notifications = notificationRepository.findByUserId(userId, size);
        } else {
//        Decode cursor
            String decodedCursor;
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            decodedCursor = new String(decodedBytes);

//            Format cursor: 26/12/2021&notificationId
//            Get createAt and notificationId from decoded cursor
            String[] parts = decodedCursor.split("&");
            String createAt = parts[0];
            String notificationId = parts[1];

            notifications = notificationRepository
                    .findByUserIdWithCursor(userId, createAt, notificationId, size);
        }

        if (notifications.isEmpty()) {
            throw new NotFoundException("Notification not found");
        }

//            Get the last data then update cursor
        Notification lastNotification = notifications.getLast();
        String newCursor = lastNotification.getCreateAt() + "&" + lastNotification.getNotificationId();

//            Encode newCursor
        String encodedCursor = Base64.getEncoder().encodeToString(newCursor.getBytes());

        List<NotificationDTO> notificationDTOs = notifications
                .stream()
                .map(NotificationDTO::new)
                .toList();

        // Create a map to return both the notifications and the cursor
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationDTOs);
        response.put("cursor", encodedCursor);

        return response;
    }

    public NotificationDTO getNotificationById(UUID id) {
        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        return new NotificationDTO(notification);
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
//        Find exist user
        User user = userRepository
                .findByUserIdAndDeleteAtIsNull(notificationDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(notificationDTO.getMessage());

        Notification savedNotification = notificationRepository.save(notification);
        return new NotificationDTO(savedNotification);
    }

    public NotificationDTO readNotification(UUID id) {
        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return new NotificationDTO(savedNotification);
    }
}
