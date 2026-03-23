package com.easystation.profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record PreferenceRecord(
    UUID id,
    String theme,
    String language,
    String layout,
    String defaultPage,
    Integer pageSize,
    String defaultSort,
    List<String> displayFields,
    List<QuickAction> quickActions,
    Boolean notificationEnabled,
    Boolean emailNotification,
    Boolean smsNotification,
    Boolean webhookNotification,
    LocalTime silentHoursStart,
    LocalTime silentHoursEnd,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record QuickAction(
        String key,
        String label,
        String icon,
        String path
    ) {}

    public record Update(
        @Size(max = 50, message = "Theme too long")
        @Pattern(regexp = "^(light|dark|auto)$", message = "Invalid theme value")
        String theme,
        
        @Size(max = 10, message = "Language code too long")
        String language,
        
        @Size(max = 50, message = "Layout too long")
        String layout,
        
        @Size(max = 255, message = "Default page too long")
        String defaultPage,
        
        @Min(value = 10, message = "Page size must be at least 10")
        @Max(value = 100, message = "Page size must be at most 100")
        Integer pageSize,
        
        @Size(max = 255, message = "Default sort too long")
        String defaultSort,
        
        List<String> displayFields,
        
        List<QuickAction> quickActions,
        
        Boolean notificationEnabled,
        Boolean emailNotification,
        Boolean smsNotification,
        Boolean webhookNotification,
        
        LocalTime silentHoursStart,
        LocalTime silentHoursEnd
    ) {}
}