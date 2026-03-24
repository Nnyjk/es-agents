package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginRecord;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.easystation.plugin.domain.enums.PluginStatus;
import io.quarkus.panache.common.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginService {

    PluginRecord create(PluginRecord.Create create);

    PluginRecord update(UUID id, PluginRecord.Update update);

    Optional<PluginRecord> findById(UUID id);

    Optional<PluginRecord> findByCode(String code);

    List<PluginRecord> search(PluginRecord.Query query);

    List<PluginRecord> findByDeveloperId(UUID developerId);

    List<PluginRecord> findByCategoryId(UUID categoryId);

    List<PluginRecord> findByStatus(PluginStatus status);

    PluginRecord publish(UUID id);

    PluginRecord suspend(UUID id, String reason);

    PluginRecord delete(UUID id);

    void incrementDownloadCount(UUID id);

    void incrementInstallCount(UUID id);

    void updateStatistics(UUID id);

    PluginRecord.Summary getSummary();

    PluginVersionRecord createVersion(UUID pluginId, PluginVersionRecord.Create create);

    PluginVersionRecord updateVersion(UUID versionId, PluginVersionRecord.Create update);

    Optional<PluginVersionRecord> findVersionById(UUID versionId);

    Optional<PluginVersionRecord> findLatestVersion(UUID pluginId);

    List<PluginVersionRecord> findVersionsByPluginId(UUID pluginId);

    PluginVersionRecord publishVersion(UUID versionId);

    PluginVersionRecord deleteVersion(UUID versionId);

    void incrementVersionDownloadCount(UUID versionId);
}