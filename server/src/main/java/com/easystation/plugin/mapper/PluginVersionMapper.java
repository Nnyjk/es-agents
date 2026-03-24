package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginVersion;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginVersionMapper {

    @Inject
    ObjectMapper objectMapper;

    public PluginVersionRecord toRecord(PluginVersion entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginVersionRecord(
            entity.id,
            entity.pluginId,
            null, // pluginName - needs to be set by service
            entity.version,
            entity.versionCode,
            entity.changelog,
            entity.downloadUrl,
            entity.packageSize,
            entity.packageHash,
            entity.signatureHash,
            entity.status,
            entity.minPlatformVersion,
            entity.maxPlatformVersion,
            parseDependenciesJson(entity.dependencies),
            entity.resourceRequirements,
            entity.downloadCount,
            entity.installCount,
            entity.isLatest,
            entity.isPrerelease,
            entity.publishedAt,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public List<PluginVersionRecord> toRecords(List<PluginVersion> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginVersion toEntity(PluginVersionRecord.Create create) {
        if (create == null) {
            return null;
        }
        
        PluginVersion entity = new PluginVersion();
        entity.pluginId = create.pluginId();
        entity.version = create.version();
        entity.versionCode = create.versionCode();
        entity.changelog = create.changelog();
        entity.downloadUrl = create.downloadUrl();
        entity.packageSize = create.packageSize();
        entity.packageHash = create.packageHash();
        entity.signatureHash = create.signatureHash();
        entity.minPlatformVersion = create.minPlatformVersion();
        entity.maxPlatformVersion = create.maxPlatformVersion();
        entity.dependencies = create.dependencies();
        entity.resourceRequirements = create.resourceRequirements();
        entity.isPrerelease = create.isPrerelease() != null ? create.isPrerelease() : false;
        entity.status = com.easystation.plugin.domain.enums.PluginStatus.DRAFT;
        entity.downloadCount = 0L;
        entity.installCount = 0L;
        entity.isLatest = false;
        
        return entity;
    }

    public void updateEntity(PluginVersion entity, PluginVersionRecord.Update update) {
        if (entity == null || update == null) {
            return;
        }
        
        if (update.version() != null) {
            entity.version = update.version();
        }
        if (update.versionCode() != null) {
            entity.versionCode = update.versionCode();
        }
        if (update.changelog() != null) {
            entity.changelog = update.changelog();
        }
        if (update.downloadUrl() != null) {
            entity.downloadUrl = update.downloadUrl();
        }
        if (update.packageSize() != null) {
            entity.packageSize = update.packageSize();
        }
        if (update.packageHash() != null) {
            entity.packageHash = update.packageHash();
        }
        if (update.signatureHash() != null) {
            entity.signatureHash = update.signatureHash();
        }
        if (update.minPlatformVersion() != null) {
            entity.minPlatformVersion = update.minPlatformVersion();
        }
        if (update.maxPlatformVersion() != null) {
            entity.maxPlatformVersion = update.maxPlatformVersion();
        }
        if (update.dependencies() != null) {
            entity.dependencies = update.dependencies();
        }
        if (update.resourceRequirements() != null) {
            entity.resourceRequirements = update.resourceRequirements();
        }
        if (update.isPrerelease() != null) {
            entity.isPrerelease = update.isPrerelease();
        }
    }

    private List<PluginVersionRecord.DependencyInfo> parseDependenciesJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PluginVersionRecord.DependencyInfo>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String dependenciesToJson(List<PluginVersionRecord.DependencyInfo> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(dependencies);
        } catch (Exception e) {
            return null;
        }
    }
}