package com.easystation.alert.dto;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class AlertRuleRecord {

    /**
     * 告警规则校验请求
     */
    public record ValidateRequest(
            String condition,
            AlertEventType eventType
    ) {}

    /**
     * 告警规则校验结果
     */
    public record ValidateResult(
            boolean valid,
            String message,
            List<String> errors
    ) {}

    public record Create(
            @NotBlank String name,
            String description,
            @NotNull AlertEventType eventType,
            @NotNull AlertLevel level,
            String condition,
            List<UUID> environmentIds,
            List<UUID> channelIds,
            Boolean enabled
    ) {}

    public record Update(
            String name,
            String description,
            AlertEventType eventType,
            AlertLevel level,
            String condition,
            List<UUID> environmentIds,
            List<UUID> channelIds,
            Boolean enabled
    ) {}

    public record Detail(
            UUID id,
            String name,
            String description,
            AlertEventType eventType,
            AlertLevel level,
            String condition,
            List<UUID> environmentIds,
            List<UUID> channelIds,
            boolean enabled,
            String createdAt,
            String updatedAt
    ) {}
}