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

    /**
     * 查询参数类（普通 class 以支持 @BeanParam）
     */
    public static class Query {
        private UUID pluginId;
        private UUID versionId;
        private ReviewStatus status;
        private String reviewType;
        private Integer page;
        private Integer size;

        public Query() {}

        public UUID getPluginId() { return pluginId; }
        public void setPluginId(UUID pluginId) { this.pluginId = pluginId; }
        public UUID getVersionId() { return versionId; }
        public void setVersionId(UUID versionId) { this.versionId = versionId; }
        public ReviewStatus getStatus() { return status; }
        public void setStatus(ReviewStatus status) { this.status = status; }
        public String getReviewType() { return reviewType; }
        public void setReviewType(String reviewType) { this.reviewType = reviewType; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }
}