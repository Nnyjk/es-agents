package com.easystation.build.dto;

import com.easystation.build.enums.BuildStatus;
import com.easystation.build.enums.BuildType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class BuildRecord {

    public record Create(
            @NotBlank String name,
            @NotNull BuildType type,
            UUID templateId,
            String config,
            String script,
            String version,
            String triggeredBy
    ) {}

    public record Update(
            String name,
            String config,
            String script
    ) {}

    public record Query(
            BuildType type,
            BuildStatus status,
            UUID templateId,
            String keyword,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}

    public record Detail(
            UUID id,
            String name,
            BuildType type,
            BuildStatus status,
            UUID templateId,
            String config,
            String script,
            String artifactPath,
            Long artifactSize,
            String version,
            String logs,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Long duration,
            String triggeredBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record ArtifactDetail(
            UUID id,
            UUID buildTaskId,
            UUID templateId,
            String name,
            String version,
            String filePath,
            Long fileSize,
            String checksum,
            String checksumType,
            boolean latest,
            int downloadCount,
            LocalDateTime createdAt
    ) {}

    public record BuildProgress(
            UUID taskId,
            BuildStatus status,
            int progress,
            String currentStep,
            String logs
    ) {}
}