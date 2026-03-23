package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.PluginInstallation;
import com.easystation.plugin.domain.PluginVersion;
import com.easystation.plugin.domain.enums.InstallationStatus;
import com.easystation.plugin.dto.PluginInstallationRecord;
import com.easystation.plugin.mapper.PluginInstallationMapper;
import com.easystation.plugin.repository.PluginInstallationRepository;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.repository.PluginVersionRepository;
import com.easystation.plugin.service.PluginInstallationService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginInstallationServiceImpl implements PluginInstallationService {

    @Inject
    PluginInstallationRepository installationRepository;

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginVersionRepository versionRepository;

    @Inject
    PluginInstallationMapper installationMapper;

    @Override
    @Transactional
    public PluginInstallationRecord install(PluginInstallationRecord.Install install, UUID userId) {
        Plugin plugin = pluginRepository.findById(install.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + install.pluginId()));

        // Get the version to install
        PluginVersion version;
        if (install.versionId() != null) {
            version = versionRepository.findById(install.versionId())
                .orElseThrow(() -> new NotFoundException("Version not found: " + install.versionId()));
        } else {
            // Get latest version
            version = versionRepository.findLatestStableByPluginId(install.pluginId())
                .orElseThrow(() -> new NotFoundException("No stable version available for plugin: " + install.pluginId()));
        }

        // Check if already installed for this agent
        if (install.agentId() != null && 
            installationRepository.existsByPluginIdAndAgentId(install.pluginId(), install.agentId())) {
            throw new BadRequestException("Plugin already installed on this agent");
        }

        PluginInstallation installation = new PluginInstallation();
        installation.setPlugin(plugin);
        installation.setVersion(version);
        installation.setAgentId(install.agentId());
        installation.setUserId(userId);
        installation.setConfigData(install.configData());
        installation.setStatus(InstallationStatus.INSTALLED);
        installation.setInstallPath(generateInstallPath(plugin.getCode(), version.getVersion()));
        installation.setCreatedAt(LocalDateTime.now());
        installation.setUpdatedAt(LocalDateTime.now());

        installationRepository.persist(installation);

        // Update plugin install count
        plugin.setTotalInstalls(plugin.getTotalInstalls() + 1);
        version.setInstallCount(version.getInstallCount() + 1);
        pluginRepository.persist(plugin);
        versionRepository.persist(version);

        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord updateConfig(UUID id, PluginInstallationRecord.UpdateConfig update) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        if (update.configData() != null) {
            installation.setConfigData(update.configData());
            installation.setUpdatedAt(LocalDateTime.now());
        }

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord start(UUID id) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        if (installation.getStatus() == InstallationStatus.DISABLED) {
            installation.setStatus(InstallationStatus.INSTALLED);
        }

        installation.setLastStartedAt(LocalDateTime.now());
        installation.setUpdatedAt(LocalDateTime.now());

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord stop(UUID id) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.setLastStoppedAt(LocalDateTime.now());
        installation.setUpdatedAt(LocalDateTime.now());

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord enable(UUID id) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        if (installation.getStatus() == InstallationStatus.DISABLED) {
            installation.setStatus(InstallationStatus.INSTALLED);
            installation.setUpdatedAt(LocalDateTime.now());
        }

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord disable(UUID id) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        if (installation.getStatus() == InstallationStatus.INSTALLED) {
            installation.setStatus(InstallationStatus.DISABLED);
            installation.setUpdatedAt(LocalDateTime.now());
        }

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord uninstall(UUID id) {
        PluginInstallation installation = installationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        Plugin plugin = installation.getPlugin();
        PluginVersion version = installation.getVersion();

        installation.setStatus(InstallationStatus.UNINSTALLED);
        installation.setUpdatedAt(LocalDateTime.now());

        installationRepository.persist(installation);

        // Update plugin install count
        if (plugin.getTotalInstalls() > 0) {
            plugin.setTotalInstalls(plugin.getTotalInstalls() - 1);
        }
        if (version.getInstallCount() > 0) {
            version.setInstallCount(version.getInstallCount() - 1);
        }
        pluginRepository.persist(plugin);
        versionRepository.persist(version);

        return installationMapper.toRecord(installation);
    }

    @Override
    public Optional<PluginInstallationRecord> findById(UUID id) {
        return installationRepository.findById(id)
            .map(installationMapper::toRecord);
    }

    @Override
    public Optional<PluginInstallationRecord> findByPluginIdAndAgentId(UUID pluginId, UUID agentId) {
        return installationRepository.findByPluginIdAndAgentId(pluginId, agentId)
            .map(installationMapper::toRecord);
    }

    @Override
    public List<PluginInstallationRecord> findByUserId(UUID userId) {
        return installationRepository.findByUserId(userId).stream()
            .map(installationMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginInstallationRecord> findByAgentId(UUID agentId) {
        return installationRepository.findByAgentId(agentId).stream()
            .map(installationMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginInstallationRecord> findByPluginId(UUID pluginId) {
        return installationRepository.findByPluginId(pluginId).stream()
            .map(installationMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginInstallationRecord> search(PluginInstallationRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.pluginId() != null) {
            queryBuilder.append(" and plugin.id = ?").append(paramIndex);
            params.add(query.pluginId());
            paramIndex++;
        }

        if (query.agentId() != null) {
            queryBuilder.append(" and agent.id = ?").append(paramIndex);
            params.add(query.agentId());
            paramIndex++;
        }

        if (query.status() != null) {
            queryBuilder.append(" and status = ?").append(paramIndex);
            params.add(query.status());
            paramIndex++;
        }

        queryBuilder.append(" ORDER BY createdAt DESC");

        int page = query.page() != null ? query.page() : 0;
        int size = query.size() != null ? query.size() : 20;

        return installationRepository.find(queryBuilder.toString(), params.toArray())
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(installationMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public PluginInstallationRecord.Summary getSummary(UUID userId) {
        List<PluginInstallation> installations = installationRepository.findByUserId(userId);
        
        int totalCount = installations.size();
        int enabledCount = (int) installations.stream()
            .filter(i -> i.getStatus() == InstallationStatus.INSTALLED)
            .count();
        int disabledCount = (int) installations.stream()
            .filter(i -> i.getStatus() == InstallationStatus.DISABLED)
            .count();
        int failedCount = (int) installations.stream()
            .filter(i -> i.getStatus() == InstallationStatus.FAILED)
            .count();

        return new PluginInstallationRecord.Summary(totalCount, enabledCount, disabledCount, failedCount);
    }

    @Override
    public long countByPluginId(UUID pluginId) {
        return installationRepository.countByPluginId(pluginId);
    }

    @Override
    public long countByAgentId(UUID agentId) {
        return installationRepository.countByAgentId(agentId);
    }

    private String generateInstallPath(String pluginCode, String version) {
        return "/plugins/" + pluginCode + "/" + version;
    }
}