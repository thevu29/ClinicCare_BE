package com.example.cliniccare.service;

import com.example.cliniccare.dto.NotificationDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Notification;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.NotificationRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        return notificationRepository.findAll().stream().map(NotificationDTO::new).toList();
    }

    public NotificationDTO getNotificationById(UUID id) {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeleteAtIsNull(id)
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
                .findByNotificationIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return new NotificationDTO(savedNotification);
    }

    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository
                .findByNotificationIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));

        notification.setDeleteAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
