package com.easystation.plugin.dto;

import com.easystation.plugin.domain.enums.InstallationStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record PluginInstallationRecord(
    UUID id,
    UUID pluginId,
    String pluginName,
    UUID versionId,
    String installedVersion,
    UUID agentId,
    String agentName,
    UUID userId,
    InstallationStatus status,
    String configData,
    String installPath,
    String errorMessage,
    LocalDateTime lastStartedAt,
    LocalDateTime lastStoppedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Install(
        @NotNull(message = "Plugin ID is required")
        UUID pluginId,

        UUID versionId,

        UUID agentId,

        String configData
    ) {}

    public record UpdateConfig(
        String configData
    ) {}

    public record BatchInstall(
        @NotEmpty(message = "Plugin IDs are required")
        java.util.List<UUID> pluginIds,

        java.util.List<UUID> agentIds
    ) {}

    public record Query(
        UUID pluginId,
        UUID agentId,
        InstallationStatus status,
        Integer page,
        Integer size
    ) {}

    public record Summary(
        int totalCount,
        int enabledCount,
        int disabledCount,
        int failedCount
    ) {}
}