package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.DeployStrategy;
import com.easystation.deployment.dto.DeployStrategyDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * DeployStrategy DTO 转换器
 */
@ApplicationScoped
public class DeployStrategyMapper {

    public DeployStrategyDTO toDTO(DeployStrategy entity) {
        if (entity == null) {
            return null;
        }
        DeployStrategyDTO dto = new DeployStrategyDTO();
        dto.id = entity.id;
        dto.applicationId = entity.applicationId;
        dto.environmentId = entity.environmentId;
        dto.name = entity.name;
        dto.type = entity.type;
        dto.deployConfig = entity.deployConfig;
        dto.healthCheckConfig = entity.healthCheckConfig;
        dto.rollbackConfig = entity.rollbackConfig;
        dto.description = entity.description;
        dto.active = entity.active;
        dto.isDefault = entity.isDefault;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;
        return dto;
    }

    public DeployStrategy toEntity(DeployStrategyDTO dto) {
        if (dto == null) {
            return null;
        }
        DeployStrategy entity = new DeployStrategy();
        entity.id = dto.id;
        entity.applicationId = dto.applicationId;
        entity.environmentId = dto.environmentId;
        entity.name = dto.name;
        entity.type = dto.type;
        entity.deployConfig = dto.deployConfig;
        entity.healthCheckConfig = dto.healthCheckConfig;
        entity.rollbackConfig = dto.rollbackConfig;
        entity.description = dto.description;
        entity.active = dto.active != null ? dto.active : true;
        entity.isDefault = dto.isDefault != null ? dto.isDefault : false;
        entity.createdAt = dto.createdAt;
        entity.updatedAt = dto.updatedAt;
        return entity;
    }

    public void updateEntity(DeployStrategy entity, DeployStrategyDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.environmentId != null) {
            entity.environmentId = dto.environmentId;
        }
        if (dto.name != null) {
            entity.name = dto.name;
        }
        if (dto.type != null) {
            entity.type = dto.type;
        }
        if (dto.deployConfig != null) {
            entity.deployConfig = dto.deployConfig;
        }
        if (dto.healthCheckConfig != null) {
            entity.healthCheckConfig = dto.healthCheckConfig;
        }
        if (dto.rollbackConfig != null) {
            entity.rollbackConfig = dto.rollbackConfig;
        }
        if (dto.description != null) {
            entity.description = dto.description;
        }
        if (dto.active != null) {
            entity.active = dto.active;
        }
        if (dto.isDefault != null) {
            entity.isDefault = dto.isDefault;
        }
    }
}