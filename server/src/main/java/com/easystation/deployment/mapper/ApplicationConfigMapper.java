package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.ApplicationConfig;
import com.easystation.deployment.dto.ApplicationConfigDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * ApplicationConfig DTO 转换器
 */
@ApplicationScoped
public class ApplicationConfigMapper {

    public ApplicationConfigDTO toDTO(ApplicationConfig entity) {
        if (entity == null) {
            return null;
        }
        ApplicationConfigDTO dto = new ApplicationConfigDTO();
        dto.id = entity.id;
        dto.applicationId = entity.applicationId;
        dto.environmentId = entity.environmentId;
        dto.configType = entity.configType;
        dto.configKey = entity.configKey;
        dto.configValue = entity.configValue;
        dto.valueType = entity.valueType;
        dto.isSecret = entity.isSecret;
        dto.description = entity.description;
        dto.active = entity.active;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;
        return dto;
    }

    public ApplicationConfig toEntity(ApplicationConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        ApplicationConfig entity = new ApplicationConfig();
        entity.id = dto.id;
        entity.applicationId = dto.applicationId;
        entity.environmentId = dto.environmentId;
        entity.configType = dto.configType;
        entity.configKey = dto.configKey;
        entity.configValue = dto.configValue;
        entity.valueType = dto.valueType;
        entity.isSecret = dto.isSecret != null ? dto.isSecret : false;
        entity.description = dto.description;
        entity.active = dto.active != null ? dto.active : true;
        entity.createdAt = dto.createdAt;
        entity.updatedAt = dto.updatedAt;
        return entity;
    }

    public void updateEntity(ApplicationConfig entity, ApplicationConfigDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.environmentId != null) {
            entity.environmentId = dto.environmentId;
        }
        if (dto.configType != null) {
            entity.configType = dto.configType;
        }
        if (dto.configKey != null) {
            entity.configKey = dto.configKey;
        }
        if (dto.configValue != null) {
            entity.configValue = dto.configValue;
        }
        if (dto.valueType != null) {
            entity.valueType = dto.valueType;
        }
        if (dto.isSecret != null) {
            entity.isSecret = dto.isSecret;
        }
        if (dto.description != null) {
            entity.description = dto.description;
        }
        if (dto.active != null) {
            entity.active = dto.active;
        }
    }
}