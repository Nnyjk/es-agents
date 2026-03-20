package com.easystation.config.service;

import com.easystation.config.domain.ConfigHistory;
import com.easystation.config.domain.ConfigItem;
import com.easystation.config.dto.ConfigRecord;
import com.easystation.config.enums.ConfigChangeType;
import com.easystation.config.enums.ConfigType;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConfigService {

    public List<ConfigRecord.Detail> list(ConfigRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.key() != null && !query.key().isBlank()) {
            sql.append(" and key like :key");
            params.put("key", "%" + query.key() + "%");
        }
        if (query.environmentId() != null) {
            sql.append(" and environmentId = :environmentId");
            params.put("environmentId", query.environmentId());
        }
        if (query.group() != null && !query.group().isBlank()) {
            sql.append(" and group = :group");
            params.put("group", query.group());
        }
        if (query.active() != null) {
            sql.append(" and active = :active");
            params.put("active", query.active());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return ConfigItem.<ConfigItem>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public ConfigRecord.Detail get(UUID id) {
        ConfigItem config = ConfigItem.findById(id);
        if (config == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return toDetail(config);
    }

    public ConfigRecord.Detail getByKey(String key, UUID environmentId) {
        StringBuilder sql = new StringBuilder("key = :key");
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);

        if (environmentId != null) {
            sql.append(" and environmentId = :environmentId");
            params.put("environmentId", environmentId);
        } else {
            sql.append(" and environmentId is null");
        }

        ConfigItem config = ConfigItem.find(sql.toString(), params).firstResult();
        if (config == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return toDetail(config);
    }

    @Transactional
    public ConfigRecord.Detail create(ConfigRecord.Create dto) {
        // Check if key already exists for this environment
        StringBuilder sql = new StringBuilder("key = :key");
        Map<String, Object> params = new HashMap<>();
        params.put("key", dto.key());

        if (dto.environmentId() != null) {
            sql.append(" and environmentId = :environmentId");
            params.put("environmentId", dto.environmentId());
        } else {
            sql.append(" and environmentId is null");
        }

        if (ConfigItem.count(sql.toString(), params) > 0) {
            throw new WebApplicationException("Config key already exists for this environment", Response.Status.CONFLICT);
        }

        ConfigItem config = new ConfigItem();
        config.key = dto.key();
        config.value = dto.value();
        config.type = dto.type();
        config.description = dto.description();
        config.environmentId = dto.environmentId();
        config.group = dto.group();
        config.encrypted = dto.encrypted() != null ? dto.encrypted() : false;
        config.active = true;
        config.version = 1;
        config.createdBy = dto.createdBy();
        config.updatedBy = dto.createdBy();
        config.persist();

        // Record history
        recordHistory(config.id, config.key, null, config.value, ConfigChangeType.CREATE.name(), config.version, dto.createdBy(), "Initial creation", dto.environmentId());

        Log.infof("Config created: %s", config.key);
        return toDetail(config);
    }

    @Transactional
    public ConfigRecord.Detail update(UUID id, ConfigRecord.Update dto) {
        ConfigItem config = ConfigItem.findById(id);
        if (config == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }

        String oldValue = config.value;

        if (dto.value() != null) config.value = dto.value();
        if (dto.type() != null) config.type = dto.type();
        if (dto.description() != null) config.description = dto.description();
        if (dto.environmentId() != null) config.environmentId = dto.environmentId();
        if (dto.group() != null) config.group = dto.group();
        if (dto.encrypted() != null) config.encrypted = dto.encrypted();
        if (dto.active() != null) config.active = dto.active();
        if (dto.updatedBy() != null) config.updatedBy = dto.updatedBy();

        config.version = config.version + 1;

        // Record history
        recordHistory(config.id, config.key, oldValue, config.value, ConfigChangeType.UPDATE.name(), config.version, dto.updatedBy(), null, config.environmentId);

        Log.infof("Config updated: %s, version: %d", config.key, config.version);
        return toDetail(config);
    }

    @Transactional
    public void delete(UUID id, String deletedBy) {
        ConfigItem config = ConfigItem.findById(id);
        if (config == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }

        // Record history before deletion
        recordHistory(config.id, config.key, config.value, null, ConfigChangeType.DELETE.name(), config.version + 1, deletedBy, "Deleted", config.environmentId);

        config.delete();
        Log.infof("Config deleted: %s", config.key);
    }

    @Transactional
    public void batchUpdate(ConfigRecord.BatchUpdate dto) {
        for (ConfigRecord.Item item : dto.items()) {
            StringBuilder sql = new StringBuilder("key = :key");
            Map<String, Object> params = new HashMap<>();
            params.put("key", item.key());

            if (item.environmentId() != null) {
                sql.append(" and environmentId = :environmentId");
                params.put("environmentId", item.environmentId());
            } else {
                sql.append(" and environmentId is null");
            }

            ConfigItem config = ConfigItem.find(sql.toString(), params).firstResult();
            if (config == null) {
                config = new ConfigItem();
                config.key = item.key();
                config.value = item.value();
                config.type = item.type() != null ? item.type() : ConfigType.STRING.name();
                config.environmentId = item.environmentId();
                config.group = item.group();
                config.encrypted = false;
                config.active = true;
                config.version = 1;
                config.createdBy = dto.updatedBy();
                config.updatedBy = dto.updatedBy();
                config.persist();
                recordHistory(config.id, config.key, null, config.value, ConfigChangeType.CREATE.name(), 1, dto.updatedBy(), "Batch creation", item.environmentId());
            } else {
                String oldValue = config.value;
                config.value = item.value();
                config.type = item.type() != null ? item.type() : config.type;
                config.group = item.group() != null ? item.group() : config.group;
                config.updatedBy = dto.updatedBy();
                config.version = config.version + 1;
                recordHistory(config.id, config.key, oldValue, config.value, ConfigChangeType.UPDATE.name(), config.version, dto.updatedBy(), "Batch update", item.environmentId());
            }
        }
        Log.infof("Batch updated %d configs", dto.items().size());
    }

    public List<ConfigRecord.HistoryDetail> getHistory(ConfigRecord.HistoryQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.configId() != null) {
            sql.append(" and configId = :configId");
            params.put("configId", query.configId());
        }
        if (query.key() != null && !query.key().isBlank()) {
            sql.append(" and key = :key");
            params.put("key", query.key());
        }
        if (query.changeType() != null && !query.changeType().isBlank()) {
            sql.append(" and changeType = :changeType");
            params.put("changeType", query.changeType());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return ConfigHistory.<ConfigHistory>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toHistoryDetail)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConfigRecord.Detail rollback(UUID historyId, ConfigRecord.RollbackRequest dto) {
        ConfigHistory history = ConfigHistory.findById(historyId);
        if (history == null) {
            throw new WebApplicationException("History not found", Response.Status.NOT_FOUND);
        }

        ConfigItem config = ConfigItem.findById(history.configId);
        if (config == null) {
            // Recreate deleted config
            config = new ConfigItem();
            config.key = history.key;
            config.environmentId = history.environmentId;
            config.createdBy = dto.changedBy();
        }

        String oldValue = config.value;
        config.value = history.oldValue;
        config.version = config.version + 1;
        config.updatedBy = dto.changedBy();
        config.active = true;
        config.persist();

        // Record rollback history
        recordHistory(config.id, config.key, oldValue, config.value, ConfigChangeType.ROLLBACK.name(), config.version, dto.changedBy(), dto.reason(), config.environmentId);

        Log.infof("Config rolled back: %s to version %d", config.key, history.version);
        return toDetail(config);
    }

    public List<ConfigRecord.EnvironmentConfig> getByEnvironment(List<UUID> environmentIds) {
        List<ConfigRecord.EnvironmentConfig> result = new ArrayList<>();

        for (UUID envId : environmentIds) {
            List<ConfigItem> configs = ConfigItem.<ConfigItem>find("environmentId = ?1 and active = ?2", envId, true).list();
            result.add(new ConfigRecord.EnvironmentConfig(
                    envId,
                    null, // Could be resolved from environment service
                    configs.stream().map(this::toDetail).collect(Collectors.toList())
            ));
        }

        return result;
    }

    public List<ConfigRecord.ConfigDiff> diff(UUID envId1, UUID envId2) {
        List<ConfigItem> configs1 = ConfigItem.<ConfigItem>find("environmentId = ?1 and active = ?2", envId1, true).list();
        List<ConfigItem> configs2 = ConfigItem.<ConfigItem>find("environmentId = ?1 and active = ?2", envId2, true).list();

        Map<String, ConfigItem> map1 = configs1.stream().collect(Collectors.toMap(c -> c.key, c -> c));
        Map<String, ConfigItem> map2 = configs2.stream().collect(Collectors.toMap(c -> c.key, c -> c));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(map1.keySet());
        allKeys.addAll(map2.keySet());

        List<ConfigRecord.ConfigDiff> diffs = new ArrayList<>();
        for (String key : allKeys) {
            ConfigItem c1 = map1.get(key);
            ConfigItem c2 = map2.get(key);

            if (c1 == null || c2 == null || !Objects.equals(c1.value, c2.value)) {
                diffs.add(new ConfigRecord.ConfigDiff(
                        key,
                        envId1,
                        c1 != null ? c1.value : null,
                        envId2,
                        c2 != null ? c2.value : null
                ));
            }
        }

        return diffs;
    }

    public List<ConfigRecord.ConfigGroup> listGroups() {
        List<Object[]> results = ConfigItem.<Object[]>find("select \"group\", count(*) from ConfigItem where active = true group by \"group\"").project(Object[].class).list();

        List<ConfigRecord.ConfigGroup> groups = new ArrayList<>();
        for (Object[] row : results) {
            String group = (String) row[0];
            Long count = (Long) row[1];
            List<ConfigItem> configs = ConfigItem.<ConfigItem>find("\"group\" = ?1 and active = ?2", group, true).list();
            groups.add(new ConfigRecord.ConfigGroup(
                    group,
                    count,
                    configs.stream().map(this::toDetail).collect(Collectors.toList())
            ));
        }

        return groups;
    }

    private void recordHistory(UUID configId, String key, String oldValue, String newValue, String changeType, Integer version, String changedBy, String reason, UUID environmentId) {
        ConfigHistory history = new ConfigHistory();
        history.configId = configId;
        history.key = key;
        history.oldValue = oldValue;
        history.newValue = newValue;
        history.changeType = changeType;
        history.version = version;
        history.changedBy = changedBy;
        history.changeReason = reason;
        history.environmentId = environmentId;
        history.persist();
    }

    private ConfigRecord.Detail toDetail(ConfigItem config) {
        return new ConfigRecord.Detail(
                config.id,
                config.key,
                config.encrypted ? "***" : config.value,
                config.type,
                config.description,
                config.environmentId,
                config.group,
                config.encrypted,
                config.active,
                config.version,
                config.createdBy,
                config.updatedBy,
                config.createdAt,
                config.updatedAt
        );
    }

    private ConfigRecord.HistoryDetail toHistoryDetail(ConfigHistory history) {
        return new ConfigRecord.HistoryDetail(
                history.id,
                history.configId,
                history.key,
                history.oldValue,
                history.newValue,
                history.changeType,
                history.version,
                history.changedBy,
                history.changeReason,
                history.environmentId,
                history.changedAt
        );
    }
}