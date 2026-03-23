package com.easystation.plugin.dto;

import com.easystation.plugin.domain.enums.PluginStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PluginVersionRecord(
    UUID id,
    UUID pluginId,
    String pluginName,
    String version,
    Integer versionCode,
    String changelog,
    String downloadUrl,
    Long packageSize,
    String packageHash,
    String signatureHash,
    PluginStatus status,
    String minPlatformVersion,
    String maxPlatformVersion,
    List<DependencyInfo> dependencies,
    String resourceRequirements,
    Long downloadCount,
    Long installCount,
    Boolean isLatest,
    Boolean isPrerelease,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record DependencyInfo(
        String name,
        String version,
        String type
    ) {}

    public record Create(
        @NotBlank(message = "Version is required")
        @Size(max = 20, message = "Version too long")
        @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$", message = "Invalid version format")
        String version,

        String changelog,

        @Size(max = 500, message = "Download URL too long")
        String downloadUrl,

        Long packageSize,

        @Size(max = 128, message = "Package hash too long")
        String packageHash,

        String dependencies,

        String resourceRequirements,

        @Size(max = 20, message = "Min platform version too long")
        String minPlatformVersion,

        @Size(max = 20, message = "Max platform version too long")
        String maxPlatformVersion,

        Boolean isPrerelease
    ) {}

    public record Query(
        UUID pluginId,
        PluginStatus status,
        Boolean isPrerelease,
        String sortBy,
        String sortOrder,
        Integer page,
        Integer size
    ) {}
}