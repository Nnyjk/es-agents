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

    /**
     * 查询参数类（普通 class 以支持 @BeanParam）
     */
    public static class Query {
        private UUID pluginId;
        private UUID agentId;
        private InstallationStatus status;
        private Integer page;
        private Integer size;

        public Query() {}

        public UUID getPluginId() { return pluginId; }
        public void setPluginId(UUID pluginId) { this.pluginId = pluginId; }
        public UUID getAgentId() { return agentId; }
        public void setAgentId(UUID agentId) { this.agentId = agentId; }
        public InstallationStatus getStatus() { return status; }
        public void setStatus(InstallationStatus status) { this.status = status; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }

    public record Summary(
        int totalCount,
        int enabledCount,
        int disabledCount,
        int failedCount
    ) {}
}