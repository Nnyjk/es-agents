package com.easystation.config.dto;

import java.time.LocalDateTime;

/**
 * 配置数据传输对象
 */
public class ConfigDTO {

    public Long id;
    public String configKey;
    public String configValue;
    public String description;
    public String configType;
    public Integer version;
    public String updatedBy;
    public LocalDateTime updatedAt;
    public LocalDateTime createdAt;

    public ConfigDTO() {}

    public static ConfigDTO fromEntity(com.easystation.config.entity.Config config) {
        ConfigDTO dto = new ConfigDTO();
        dto.id = config.id;
        dto.configKey = config.configKey;
        dto.configValue = config.configValue;
        dto.description = config.description;
        dto.configType = config.configType;
        dto.version = config.version;
        dto.updatedBy = config.updatedBy;
        dto.updatedAt = config.updatedAt;
        dto.createdAt = config.createdAt;
        return dto;
    }
}
