package com.easystation.config.service;

import com.easystation.config.dto.ConfigDTO;
import com.easystation.config.dto.ConfigHistoryDTO;
import com.easystation.config.dto.ConfigUpdateRequest;
import com.easystation.config.entity.Config;
import com.easystation.config.entity.ConfigHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置服务
 * 
 * 提供配置的 CRUD 操作和热重载支持
 */
@ApplicationScoped
public class ConfigService {

    private static final Logger log = Logger.getLogger(ConfigService.class);

    /**
     * 获取所有配置
     */
    public List<ConfigDTO> getAllConfigs() {
        return Config.<Config>listAll()
            .stream()
            .map(ConfigDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * 根据 key 获取配置
     */
    public ConfigDTO getConfigByKey(String configKey) {
        Config config = Config.findByKey(configKey);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在：" + configKey);
        }
        return ConfigDTO.fromEntity(config);
    }

    /**
     * 获取配置值（快捷方法）
     */
    public String getValue(String configKey, String defaultValue) {
        Config config = Config.findByKey(configKey);
        return config != null ? config.configValue : defaultValue;
    }

    /**
     * 更新配置
     */
    @Transactional
    public ConfigDTO updateConfig(String configKey, ConfigUpdateRequest request, String currentUser) {
        Config config = Config.findByKey(configKey);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在：" + configKey);
        }

        String oldValue = config.configValue;
        String newValue = request.configValue;

        // 如果值没有变化，直接返回
        if (oldValue.equals(newValue)) {
            log.infof("配置值未变化：%s", configKey);
            return ConfigDTO.fromEntity(config);
        }

        // 更新配置
        config.configValue = newValue;
        config.updatedBy = currentUser;
        config.updatedAt = LocalDateTime.now();
        config.version = (config.version != null ? config.version : 0) + 1;
        config.persist();

        // 记录历史
        recordHistory(config.id, oldValue, newValue, currentUser, request.changeReason);

        log.infof("配置已更新：%s", configKey);

        // TODO: 发布 Redis Pub/Sub 消息通知其他实例
        // publishConfigChange(configKey);

        return ConfigDTO.fromEntity(config);
    }

    /**
     * 创建配置
     */
    @Transactional
    public ConfigDTO createConfig(String configKey, ConfigUpdateRequest request, String currentUser) {
        // 检查是否已存在
        Config existing = Config.findByKey(configKey);
        if (existing != null) {
            throw new IllegalArgumentException("配置已存在：" + configKey);
        }

        Config config = new Config();
        config.configKey = configKey;
        config.configValue = request.configValue;
        config.description = request.description;
        config.configType = request.configType;
        config.version = 1;
        config.updatedBy = currentUser;
        config.updatedAt = LocalDateTime.now();
        config.createdAt = LocalDateTime.now();
        config.persist();

        log.infof("配置已创建：%s", configKey);

        return ConfigDTO.fromEntity(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(String configKey) {
        Config config = Config.findByKey(configKey);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在：" + configKey);
        }
        config.delete();
        log.infof("配置已删除：%s", configKey);
    }

    /**
     * 获取配置历史
     */
    public List<ConfigHistoryDTO> getHistory(String configKey) {
        Config config = Config.findByKey(configKey);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在：" + configKey);
        }
        return ConfigHistory.findByConfigId(config.id)
            .stream()
            .map(ConfigHistoryDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * 记录配置变更历史
     */
    @Transactional
    void recordHistory(Long configId, String oldValue, String newValue, 
                       String changedBy, String reason) {
        ConfigHistory history = new ConfigHistory();
        history.configId = configId;
        history.oldValue = oldValue;
        history.newValue = newValue;
        history.changedBy = changedBy;
        history.changedAt = LocalDateTime.now();
        history.changeReason = reason;
        history.persist();
    }

    /**
     * 发布配置变更消息（Redis Pub/Sub）
     * 
     * TODO: 实现 Redis Pub/Sub 消息发布
     */
    void publishConfigChange(String configKey) {
        log.infof("配置变更通知：%s", configKey);
        // 待实现：使用 Redis Pub/Sub 发布消息
    }
}
