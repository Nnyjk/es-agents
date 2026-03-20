package com.easystation.config.dto;

import com.easystation.config.enums.ConfigType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigRecord {

    public record Create(
            @NotBlank String key,
            String value,
            @NotBlank String type,
            String description,
            UUID environmentId,
            String group,
            Boolean encrypted,
            String createdBy
    ) {}

    public record Update(
            String value,
            String type,
            String description,
            UUID environmentId,
            String group,
            Boolean encrypted,
            Boolean active,
            String updatedBy
    ) {}

    public record Detail(
            UUID id,
            String key,
            String value,
            String type,
            String description,
            UUID environmentId,
            String group,
            boolean encrypted,
            boolean active,
            Integer version,
            String createdBy,
            String updatedBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record Query(
            String key,
            UUID environmentId,
            String group,
            Boolean active,
            Integer limit,
            Integer offset
    ) {}

    public record BatchUpdate(
            List<Item> items,
            String updatedBy
    ) {}

    public record Item(
            String key,
            String value,
            String type,
            UUID environmentId,
            String group
    ) {}

    public record HistoryDetail(
            UUID id,
            UUID configId,
            String key,
            String oldValue,
            String newValue,
            String changeType,
            Integer version,
            String changedBy,
            String changeReason,
            UUID environmentId,
            LocalDateTime changedAt
    ) {}

    public record HistoryQuery(
            UUID configId,
            String key,
            String changeType,
            Integer limit,
            Integer offset
    ) {}

    public record RollbackRequest(
            String changedBy,
            String reason
    ) {}

    public record EnvironmentConfig(
            UUID environmentId,
            String environmentName,
            List<Detail> configs
    ) {}

    public record ConfigDiff(
            String key,
            UUID environmentId1,
            String value1,
            UUID environmentId2,
            String value2
    ) {}

    public record ConfigGroup(
            String group,
            long count,
            List<Detail> configs
    ) {}
}