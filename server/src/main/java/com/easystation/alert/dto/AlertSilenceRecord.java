package com.easystation.alert.dto;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AlertSilenceRecord {

    public record Create(
            @NotBlank String name,
            String description,
            String matchCondition,
            LocalDateTime silenceStart,
            LocalDateTime silenceEnd,
            Integer durationSeconds,
            Boolean enabled
    ) {}

    public record Update(
            String name,
            String description,
            String matchCondition,
            LocalDateTime silenceStart,
            LocalDateTime silenceEnd,
            Integer durationSeconds,
            Boolean enabled
    ) {}

    public record Detail(
            UUID id,
            String name,
            String description,
            String matchCondition,
            LocalDateTime silenceStart,
            LocalDateTime silenceEnd,
            Integer durationSeconds,
            boolean enabled,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    /**
     * 静默匹配条件
     */
    public record MatchCondition(
            List<AlertEventType> eventTypes,
            List<AlertLevel> levels,
            List<String> sources,
            List<String> tags,
            String customCondition
    ) {}
}