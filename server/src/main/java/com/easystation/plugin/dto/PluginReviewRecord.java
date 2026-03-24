package com.easystation.plugin.dto;

import com.easystation.plugin.domain.enums.ReviewStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record PluginReviewRecord(
    UUID id,
    UUID pluginId,
    String pluginName,
    UUID versionId,
    String version,
    UUID reviewerId,
    String reviewerName,
    ReviewStatus status,
    String reviewType,
    String comment,
    String securityCheckResult,
    String compatibilityCheckResult,
    String testReport,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotNull(message = "Plugin ID is required")
        UUID pluginId,

        UUID versionId,

        @NotBlank(message = "Review type is required")
        @Size(max = 20, message = "Review type too long")
        String reviewType,

        String comment
    ) {}

    public record Submit(
        @NotNull(message = "Plugin ID is required")
        UUID pluginId,

        UUID versionId,

        @NotBlank(message = "Review type is required")
        @Size(max = 20, message = "Review type too long")
        String reviewType,

        String comment
    ) {}

    public record Approve(
        String comment,
        String securityCheckResult,
        String compatibilityCheckResult,
        String testReport
    ) {}

    public record Reject(
        @NotBlank(message = "Rejection reason is required")
        String comment
    ) {}

    public record Query(
        UUID pluginId,
        UUID versionId,
        ReviewStatus status,
        String reviewType,
        Integer page,
        Integer size
    ) {}
}