package com.example.cliniccare.service;

import com.example.cliniccare.dto.NotificationDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Notification;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.NotificationRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    public List<NotificationDTO> getNotifications() {
        try {
            return notificationRepository.findAll().stream().map(NotificationDTO::new).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get notifications", e);
        }
    }

    public NotificationDTO getNotificationById(UUID id)
            throws NotFoundException {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        return new NotificationDTO(notification);
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO)
            throws NotFoundException {
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

    public NotificationDTO updateNotification(
            UUID id, NotificationDTO notificationDTO)
            throws NotFoundException {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

//      If want to update user's notification
        if (notificationDTO.getUserId() != null) {
//            Find exist user
            User user = userRepository
                    .findByUserIdAndDeleteAtIsNull(notificationDTO.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            notification.setUser(user);
        }
//        If want to update notification's message
        if (notificationDTO.getMessage() != null) {
            notification.setMessage(notificationDTO.getMessage());
        }

        return new NotificationDTO(notificationRepository.save(notification));
    }

    public void deleteNotification(UUID id) throws NotFoundException {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setDeleteAt(new Date());
        notificationRepository.save(notification);
    }
}