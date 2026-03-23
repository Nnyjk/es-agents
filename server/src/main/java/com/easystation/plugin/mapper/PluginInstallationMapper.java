package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginInstallation;
import com.easystation.plugin.dto.PluginInstallationRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginInstallationMapper {

    public PluginInstallationRecord toRecord(PluginInstallation entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginInstallationRecord(
            entity.id,
            entity.pluginId,
            null, // pluginName - needs to be set by service
            entity.versionId,
            null, // installedVersion - needs to be set by service
            entity.agentId,
            null, // agentName - needs to be set by service
            entity.userId,
            entity.status,
            entity.configData,
            entity.installPath,
            entity.errorMessage,
            entity.lastStartedAt,
            entity.lastStoppedAt,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public List<PluginInstallationRecord> toRecords(List<PluginInstallation> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginInstallation toEntity(PluginInstallationRecord.Install install, UUID userId) {
        if (install == null) {
            return null;
        }
        
        PluginInstallation entity = new PluginInstallation();
        entity.pluginId = install.pluginId();
        entity.versionId = install.versionId();
        entity.agentId = install.agentId();
        entity.userId = userId;
        entity.configData = install.configData();
        entity.status = com.easystation.plugin.domain.enums.InstallationStatus.INSTALLING;
        
        return entity;
    }
}