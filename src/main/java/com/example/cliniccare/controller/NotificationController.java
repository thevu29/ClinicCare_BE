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
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ResponseEntity<?> handleValidate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, errors, null
            ));
        }
        return null;
    }

    @GetMapping
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

    @PostMapping
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody NotificationDTO notificationDTO,
            BindingResult bindingResult) {
        try {
            if (handleValidate(bindingResult) != null) {
                return handleValidate(bindingResult);
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
                    .body(new ApiResponse<>(false, e.getMessage(), null));
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
