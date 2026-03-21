package com.easystation.alert.dto;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class AlertRuleRecord {

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