package com.example.cliniccare.controller;

import com.example.cliniccare.dto.NotificationDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.NotificationService;
import com.example.cliniccare.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

//    Get Notification by User Id with Cursor Pagination
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam UUID userId
    ) {
        try {
            Map<String, Object> notifications = notificationService
                    .getNotifications(cursor, userId, size);

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

    @PostMapping
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody NotificationDTO notificationDTO,
            BindingResult bindingResult) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
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

    @PutMapping("/read/{id}")
    public ResponseEntity<?> readNotification(@PathVariable UUID id) {
        try {
            NotificationDTO notification = notificationService.readNotification(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Read notification successfully", notification
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to read notification: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                    false, "Failed to read notification", null
            ));
        }
    }
}
