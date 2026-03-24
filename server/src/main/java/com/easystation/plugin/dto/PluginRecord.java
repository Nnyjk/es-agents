package com.easystation.plugin.dto;

import com.easystation.plugin.domain.enums.PluginStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PluginRecord(
    UUID id,
    UUID developerId,
    String developerName,
    UUID categoryId,
    String categoryName,
    String name,
    String code,
    String icon,
    String description,
    String readme,
    String sourceUrl,
    String docUrl,
    String homepageUrl,
    PluginStatus status,
    Boolean isFree,
    BigDecimal price,
    String minPlatformVersion,
    String maxPlatformVersion,
    List<String> supportedPlatforms,
    List<String> permissionsRequired,
    String configSchema,
    Long totalDownloads,
    Long totalInstalls,
    BigDecimal averageRating,
    Integer ratingCount,
    Integer commentCount,
    Integer favoriteCount,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<String> tags,
    PluginVersionInfo latestVersion
) {
    public record PluginVersionInfo(
        UUID id,
        String version,
        String downloadUrl,
        Long packageSize,
        LocalDateTime publishedAt
    ) {}

    public record Create(
        @NotNull(message = "Developer ID is required")
        UUID developerId,

        @NotBlank(message = "Plugin name is required")
        @Size(max = 100, message = "Plugin name too long")
        String name,

        @Size(max = 50, message = "Plugin code too long")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Code must be lowercase alphanumeric with hyphens")
        String code,

        UUID categoryId,

        @Size(max = 500, message = "Icon URL too long")
        String icon,

        @Size(max = 2000, message = "Description too long")
        String description,

        String readme,

        @Size(max = 500, message = "Source URL too long")
        String sourceUrl,

        @Size(max = 500, message = "Doc URL too long")
        String docUrl,

        @Size(max = 500, message = "Homepage URL too long")
        String homepageUrl,

        Boolean isFree,

        BigDecimal price,

        @Size(max = 20, message = "Min platform version too long")
        String minPlatformVersion,

        @Size(max = 20, message = "Max platform version too long")
        String maxPlatformVersion,

        List<String> supportedPlatforms,

        List<String> permissionsRequired,

        String configSchema,

        List<String> tags
    ) {}

    public record Update(
        @Size(max = 100, message = "Plugin name too long")
        String name,

        UUID categoryId,

        @Size(max = 500, message = "Icon URL too long")
        String icon,

        @Size(max = 2000, message = "Description too long")
        String description,

        String readme,

        @Size(max = 500, message = "Source URL too long")
        String sourceUrl,

        @Size(max = 500, message = "Doc URL too long")
        String docUrl,

        @Size(max = 500, message = "Homepage URL too long")
        String homepageUrl,

        Boolean isFree,

        BigDecimal price,

        String minPlatformVersion,

        String maxPlatformVersion,

        List<String> supportedPlatforms,

        List<String> permissionsRequired,

        String configSchema,

        List<String> tags
    ) {}

    public record Query(
        String keyword,
        UUID categoryId,
        String tag,
        PluginStatus status,
        UUID developerId,
        Boolean isFree,
        String sortBy,
        String sortOrder,
        Integer page,
        Integer size
    ) {}

    public record Summary(
        long totalCount,
        long publishedCount,
        long pendingCount,
        long totalDownloads
    ) {}
}