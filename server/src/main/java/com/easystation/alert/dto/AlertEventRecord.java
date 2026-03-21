package com.easystation.alert.dto;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AlertEventRecord {

    public record Create(
            @NotNull AlertEventType eventType,
            @NotNull AlertLevel level,
            @NotBlank String title,
            String message,
            UUID resourceId,
            String resourceType,
            UUID environmentId,
            UUID ruleId
    ) {}

    public record Query(
            AlertEventType eventType,
            AlertLevel level,
            AlertStatus status,
            UUID environmentId,
            UUID resourceId,
            String resourceType,
            String keyword,
            Integer limit,
            Integer offset
    ) {}

    public record Detail(
            UUID id,
            AlertEventType eventType,
            AlertLevel level,
            AlertStatus status,
            String title,
            String message,
            UUID resourceId,
            String resourceType,
            UUID environmentId,
            UUID ruleId,
            int count,
            String lastNotifiedAt,
            String acknowledgedBy,
            String acknowledgedAt,
            String resolvedBy,
            String resolvedAt,
            String createdAt,
            String updatedAt
    ) {}

    public record Acknowledge(
            String acknowledgedBy
    ) {}

    public record Resolve(
            String resolvedBy
    ) {}
}