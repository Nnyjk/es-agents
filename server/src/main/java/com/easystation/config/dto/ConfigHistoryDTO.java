package com.easystation.config.dto;

import java.time.LocalDateTime;

/**
 * 配置历史 DTO
 */
public class ConfigHistoryDTO {

    public Long id;
    public Long configId;
    public String oldValue;
    public String newValue;
    public String changedBy;
    public LocalDateTime changedAt;
    public String changeReason;

    public ConfigHistoryDTO() {}

    public static ConfigHistoryDTO fromEntity(com.easystation.config.entity.ConfigHistory history) {
        ConfigHistoryDTO dto = new ConfigHistoryDTO();
        dto.id = history.id;
        dto.configId = history.configId;
        dto.oldValue = history.oldValue;
        dto.newValue = history.newValue;
        dto.changedBy = history.changedBy;
        dto.changedAt = history.changedAt;
        dto.changeReason = history.changeReason;
        return dto;
    }
}
