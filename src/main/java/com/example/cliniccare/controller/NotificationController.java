package com.example.cliniccare.controller;

import com.example.cliniccare.dto.NotificationDTO;
import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.NotificationService;
import com.example.cliniccare.service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getNotifications();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get notifications successfully", notifications
            ));
        } catch (Exception e) {
            logger.error("Failed to get notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable UUID id) {
        try {
            NotificationDTO notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get notification successfully", notification
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to get notification: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody NotificationDTO notificationDTO,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }

            NotificationDTO notification = notificationService.createNotification(notificationDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(
                            true, "Create notification successfully", notification
                    ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to create notification: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                    false, "Failed to create notification", null
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNotification(@PathVariable UUID id,
                                        @RequestBody NotificationDTO notificationDTO) {
        try {
            NotificationDTO notification = notificationService
                    .updateNotification(id, notificationDTO);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update notification successfully", notification
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to update notification: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                    false, "Failed to update notification", null
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable UUID id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete notification successfully", null
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to delete notification: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                    false, "Failed to delete notification", null
            ));
        }
    }
}
