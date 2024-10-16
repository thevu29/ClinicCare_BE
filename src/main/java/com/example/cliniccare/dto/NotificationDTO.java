package com.example.cliniccare.dto;

import com.example.cliniccare.model.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class NotificationDTO {
    private UUID notificationId;

    @NotNull(message = "UserId is required")
    private UUID userId;

    @NotBlank(message = "Message is required")
    private String message;

    private Date createAt;

    private Date deleteAt;

    public NotificationDTO() {
    }

    public NotificationDTO(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.userId = notification.getUser().getUserId();
        this.message = notification.getMessage();
        this.createAt = notification.getCreateAt();
        this.deleteAt = notification.getDeleteAt();
    }
}
