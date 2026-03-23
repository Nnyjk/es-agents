package com.easystation.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NotificationSubscriptionRecord(
    UUID id,
    String notificationType,
    Boolean enabled,
    List<String> channels,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Update(
        @NotBlank(message = "Notification type is required")
        @Size(max = 100, message = "Notification type too long")
        String notificationType,
        
        Boolean enabled,
        
        List<@Size(max = 50, message = "Channel name too long") String> channels
    ) {}

    public record BatchUpdate(
        List<Update> subscriptions
    ) {}
}