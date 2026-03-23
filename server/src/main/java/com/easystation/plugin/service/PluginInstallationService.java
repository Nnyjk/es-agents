package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginInstallationRecord;
import com.easystation.plugin.domain.enums.InstallationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginInstallationService {

    PluginInstallationRecord install(PluginInstallationRecord.Install install, UUID userId);

    PluginInstallationRecord updateConfig(UUID id, PluginInstallationRecord.UpdateConfig update);

    PluginInstallationRecord start(UUID id);

    PluginInstallationRecord stop(UUID id);

    PluginInstallationRecord enable(UUID id);

    PluginInstallationRecord disable(UUID id);

    PluginInstallationRecord uninstall(UUID id);

    Optional<PluginInstallationRecord> findById(UUID id);

    Optional<PluginInstallationRecord> findByPluginIdAndAgentId(UUID pluginId, UUID agentId);

    List<PluginInstallationRecord> findByUserId(UUID userId);

    List<PluginInstallationRecord> findByAgentId(UUID agentId);

    List<PluginInstallationRecord> findByPluginId(UUID pluginId);

    List<PluginInstallationRecord> search(PluginInstallationRecord.Query query);

    PluginInstallationRecord.Summary getSummary(UUID userId);

    long countByPluginId(UUID pluginId);

    long countByAgentId(UUID agentId);
}