package com.easystation.plugin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PluginRatingRecord(
    UUID id,
    UUID pluginId,
    String pluginName,
    UUID userId,
    String userName,
    BigDecimal rating,
    String review,
    Boolean isVerified,
    Integer helpfulCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotNull(message = "Plugin ID is required")
        UUID pluginId,

        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
        @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
        BigDecimal rating,

        @Size(max = 2000, message = "Review too long")
        String review
    ) {}

    public record Update(
        @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
        @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
        BigDecimal rating,

        @Size(max = 2000, message = "Review too long")
        String review
    ) {}

    /**
     * 查询参数类（普通 class 以支持 @BeanParam）
     */
    public static class Query {
        private UUID pluginId;
        private UUID userId;
        private Boolean verified;
        private String sortBy;
        private String sortOrder;
        private Integer page;
        private Integer size;

        public Query() {}

        public UUID getPluginId() { return pluginId; }
        public void setPluginId(UUID pluginId) { this.pluginId = pluginId; }
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public Boolean getVerified() { return verified; }
        public void setVerified(Boolean verified) { this.verified = verified; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }

    public record Summary(
        BigDecimal averageRating,
        int totalCount,
        int[] distribution
    ) {}
}