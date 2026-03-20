package com.easystation.pipeline.dto;

import com.easystation.pipeline.enums.PipelineStatus;
import com.easystation.pipeline.enums.ExecutionStatus;
import com.easystation.pipeline.enums.StageType;
import com.easystation.pipeline.enums.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PipelineRecord {

    public record Create(
            @NotBlank String name,
            String description,
            UUID environmentId,
            UUID templateId,
            List<StageConfig> stages,
            String triggerConfig
    ) {}

    public record Update(
            String name,
            String description,
            PipelineStatus status,
            UUID environmentId,
            UUID templateId,
            List<StageConfig> stages,
            String triggerConfig,
            Boolean enabled
    ) {}

    public record StageConfig(
            StageType type,
            String name,
            int order,
            String config
    ) {}

    public record Detail(
            UUID id,
            String name,
            String description,
            PipelineStatus status,
            UUID environmentId,
            UUID templateId,
            List<StageConfig> stages,
            String triggerConfig,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record ExecutionCreate(
            @NotNull UUID pipelineId,
            @NotNull TriggerType triggerType,
            String triggeredBy,
            String version
    ) {}

    public record ExecutionDetail(
            UUID id,
            UUID pipelineId,
            ExecutionStatus status,
            TriggerType triggerType,
            String triggeredBy,
            UUID deploymentId,
            String version,
            String logs,
            String errorMessage,
            int currentStage,
            int totalStages,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Long duration,
            LocalDateTime createdAt
    ) {}

    public record StageDetail(
            UUID id,
            UUID executionId,
            StageType type,
            String name,
            int orderIndex,
            ExecutionStatus status,
            String config,
            String logs,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Long duration
    ) {}

    public record Query(
            PipelineStatus status,
            UUID environmentId,
            String keyword,
            Integer limit,
            Integer offset
    ) {}

    public record ExecutionQuery(
            UUID pipelineId,
            ExecutionStatus status,
            TriggerType triggerType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}
}