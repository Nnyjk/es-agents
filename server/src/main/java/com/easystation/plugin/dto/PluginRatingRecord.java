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

    public record Query(
        UUID pluginId,
        UUID userId,
        Boolean verified,
        String sortBy,
        String sortOrder,
        Integer page,
        Integer size
    ) {}

    public record Summary(
        BigDecimal averageRating,
        int totalCount,
        int[] distribution
    ) {}
}