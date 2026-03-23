package com.easystation.plugin.repository;

import com.easystation.plugin.domain.entity.PluginInstallation;
import com.easystation.plugin.domain.enums.InstallationStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginInstallationRepository implements PanacheRepositoryBase<PluginInstallation, UUID> {

    public List<PluginInstallation> findByPluginId(UUID pluginId) {
        return list("plugin.id", pluginId);
    }

    public List<PluginInstallation> findByAgentId(UUID agentId) {
        return list("agent.id", agentId);
    }

    public List<PluginInstallation> findByUserId(UUID userId) {
        return list("user.id", userId);
    }

    public List<PluginInstallation> findByStatus(InstallationStatus status) {
        return list("status", status);
    }

    public List<PluginInstallation> findByPluginIdAndStatus(UUID pluginId, InstallationStatus status) {
        return list("plugin.id = ?1 and status = ?2", pluginId, status);
    }

    public List<PluginInstallation> findByAgentIdAndStatus(UUID agentId, InstallationStatus status) {
        return list("agent.id = ?1 and status = ?2", agentId, status);
    }

    public Optional<PluginInstallation> findByPluginIdAndAgentId(UUID pluginId, UUID agentId) {
        return find("plugin.id = ?1 and agent.id = ?2", pluginId, agentId).firstResultOptional();
    }

    public Optional<PluginInstallation> findByPluginIdAndUserId(UUID pluginId, UUID userId) {
        return find("plugin.id = ?1 and user.id = ?2", pluginId, userId).firstResultOptional();
    }

    public long countByPluginId(UUID pluginId) {
        return count("plugin.id", pluginId);
    }

    public long countByAgentId(UUID agentId) {
        return count("agent.id", agentId);
    }

    public long countByUserId(UUID userId) {
        return count("user.id", userId);
    }

    public long countByStatus(InstallationStatus status) {
        return count("status", status);
    }

    public long countByPluginIdAndStatus(UUID pluginId, InstallationStatus status) {
        return count("plugin.id = ?1 and status = ?2", pluginId, status);
    }

    public boolean existsByPluginIdAndAgentId(UUID pluginId, UUID agentId) {
        return count("plugin.id = ?1 and agent.id = ?2", pluginId, agentId) > 0;
    }
}