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
        // Validate plugin exists
        Plugin plugin = pluginRepository.findByIdOptional(install.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + install.pluginId()));

        // Validate version if specified
        PluginVersion version = null;
        if (install.versionId() != null) {
            version = versionRepository.findByIdOptional(install.versionId())
                .orElseThrow(() -> new NotFoundException("Version not found: " + install.versionId()));
        }

        // Check if already installed for this user/agent
        if (install.agentId() != null) {
            Optional<PluginInstallation> existing = installationRepository.findByPluginIdAndAgentId(
                install.pluginId(), install.agentId());
            if (existing.isPresent()) {
                throw new BadRequestException("Plugin already installed for this agent");
            }
        }

        PluginInstallation installation = new PluginInstallation();
        installation.setPluginId(plugin.id);
        installation.setVersionId(version != null ? version.id : null);
        installation.setAgentId(install.agentId());
        installation.setUserId(userId);
        installation.setStatus(InstallationStatus.INSTALLING);
        installation.setInstalledVersion(version != null ? version.version : null);
        installation.setConfigData(install.configData());
        installation.setCreatedAt(LocalDateTime.now());
        installation.setUpdatedAt(LocalDateTime.now());

        installationRepository.persist(installation);

        // Update plugin install count
        plugin.totalInstalls = plugin.totalInstalls + 1;
        pluginRepository.persist(plugin);

        // Update version install count
        if (version != null) {
            version.installCount = version.installCount + 1;
            versionRepository.persist(version);
        }

        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public List<PluginInstallationRecord> batchInstall(PluginInstallationRecord.BatchInstall batchInstall, UUID userId) {
        return batchInstall.pluginIds().stream()
            .map(pluginId -> {
                PluginInstallationRecord.Install install = new PluginInstallationRecord.Install(
                    pluginId,
                    null,
                    batchInstall.agentIds() != null && !batchInstall.agentIds().isEmpty() 
                        ? batchInstall.agentIds().get(0) 
                        : null,
                    null
                );
                return install(install, userId);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PluginInstallationRecord updateConfig(UUID id, PluginInstallationRecord.UpdateConfig update) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        if (update.configData() != null) {
            installation.configData = update.configData();
        }
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord start(UUID id) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.status = InstallationStatus.RUNNING;
        installation.lastStartedAt = LocalDateTime.now();
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord stop(UUID id) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.status = InstallationStatus.STOPPED;
        installation.lastStoppedAt = LocalDateTime.now();
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord enable(UUID id) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.status = InstallationStatus.RUNNING;
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord disable(UUID id) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.status = InstallationStatus.STOPPED;
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);
        return installationMapper.toRecord(installation);
    }

    @Override
    @Transactional
    public PluginInstallationRecord uninstall(UUID id) {
        PluginInstallation installation = installationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Installation not found: " + id));

        installation.status = InstallationStatus.UNINSTALLED;
        installation.updatedAt = LocalDateTime.now();

        installationRepository.persist(installation);

        // Update plugin install count
        pluginRepository.findByIdOptional(installation.pluginId).ifPresent(plugin -> {
            plugin.totalInstalls = plugin.totalInstalls - 1;
            pluginRepository.persist(plugin);
        });

        // Update version install count
        if (installation.versionId != null) {
            versionRepository.findByIdOptional(installation.versionId).ifPresent(version -> {
                version.installCount = version.installCount - 1;
                versionRepository.persist(version);
            });
        }

        return installationMapper.toRecord(installation);
    }

    @Override
    public Optional<PluginInstallationRecord> findById(UUID id) {
        return installationRepository.findByIdOptional(id)
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
        List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.pluginId() != null) {
            queryBuilder.append(" and pluginId = ?").append(paramIndex);
            params.add(query.pluginId());
            paramIndex++;
        }

        if (query.agentId() != null) {
            queryBuilder.append(" and agentId = ?").append(paramIndex);
            params.add(query.agentId());
            paramIndex++;
        }

        if (query.status() != null) {
            queryBuilder.append(" and status = ?").append(paramIndex);
            params.add(query.status());
            paramIndex++;
        }

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
    public long countByPluginId(UUID pluginId) {
        return installationRepository.countByPluginId(pluginId);
    }

    @Override
    public long countByAgentId(UUID agentId) {
        return installationRepository.countByAgentId(agentId);
    }

    @Override
    public PluginInstallationRecord.Summary getSummary(UUID userId) {
        long totalInstallations = installationRepository.countByUserId(userId);
        long enabledCount = installationRepository.findByUserId(userId).stream()
            .filter(i -> i.status == InstallationStatus.ENABLED)
            .count();
        long disabledCount = installationRepository.findByUserId(userId).stream()
            .filter(i -> i.status == InstallationStatus.DISABLED)
            .count();
        long failedCount = installationRepository.findByUserId(userId).stream()
            .filter(i -> i.status == InstallationStatus.FAILED)
            .count();

        return new PluginInstallationRecord.Summary(
            (int) totalInstallations,
            (int) enabledCount,
            (int) disabledCount,
            (int) failedCount
        );
    }
}