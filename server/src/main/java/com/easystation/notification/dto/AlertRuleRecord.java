package com.easystation.notification.dto;

import com.easystation.notification.enums.ConditionType;
import com.easystation.notification.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AlertRuleRecord(
    UUID id,
    String name,
    String metric,
    ConditionType conditionType,
    String threshold,
    Severity severity,
    List<UUID> notificationChannelIds,
    Boolean enabled,
    UUID createdBy,
    LocalDateTime createdAt
) {
    public record Create(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Metric cannot be blank")
        String metric,
        @NotNull(message = "Condition type cannot be null")
        ConditionType conditionType,
        @NotBlank(message = "Threshold cannot be blank")
        String threshold,
        @NotNull(message = "Severity cannot be null")
        Severity severity,
        List<UUID> notificationChannelIds,
        Boolean enabled
    ) {}

    public record Update(
        String name,
        String metric,
        ConditionType conditionType,
        String threshold,
        Severity severity,
        List<UUID> notificationChannelIds,
        Boolean enabled
    ) {}
}