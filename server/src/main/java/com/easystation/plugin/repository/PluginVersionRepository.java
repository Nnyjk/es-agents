package com.easystation.plugin.repository;

import com.easystation.plugin.domain.entity.PluginVersion;
import com.easystation.plugin.domain.enums.PluginStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginVersionRepository implements PanacheRepositoryBase<PluginVersion, UUID> {

    public Optional<PluginVersion> findByPluginIdAndVersion(UUID pluginId, String version) {
        return find("plugin.id = ?1 and version = ?2", pluginId, version).firstResultOptional();
    }

    public List<PluginVersion> findByPluginId(UUID pluginId) {
        return list("plugin.id", pluginId);
    }

    public List<PluginVersion> findByPluginIdOrderByVersionCodeDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY versionCode DESC", pluginId);
    }

    public List<PluginVersion> findByPluginIdAndStatus(UUID pluginId, PluginStatus status) {
        return list("plugin.id = ?1 and status = ?2", pluginId, status);
    }

    public List<PluginVersion> findByPluginIdAndIsPrerelease(UUID pluginId, Boolean isPrerelease) {
        return list("plugin.id = ?1 and isPrerelease = ?2", pluginId, isPrerelease);
    }

    public Optional<PluginVersion> findLatestByPluginId(UUID pluginId) {
        return find("plugin.id = ?1 and isLatest = true", pluginId).firstResultOptional();
    }

    public Optional<PluginVersion> findLatestStableByPluginId(UUID pluginId) {
        return find("plugin.id = ?1 and isLatest = true and isPrerelease = false", pluginId).firstResultOptional();
    }

    public long countByPluginId(UUID pluginId) {
        return count("plugin.id", pluginId);
    }

    public boolean existsByPluginIdAndVersion(UUID pluginId, String version) {
        return count("plugin.id = ?1 and version = ?2", pluginId, version) > 0;
    }

    public void clearLatestFlag(UUID pluginId) {
        update("isLatest = false where plugin.id = ?1", pluginId);
    }
}