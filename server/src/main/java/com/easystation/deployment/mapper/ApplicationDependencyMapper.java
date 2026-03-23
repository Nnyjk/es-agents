package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.ApplicationDependency;
import com.easystation.deployment.dto.ApplicationDependencyDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * ApplicationDependency DTO 转换器
 */
@ApplicationScoped
public class ApplicationDependencyMapper {

    public ApplicationDependencyDTO toDTO(ApplicationDependency entity) {
        if (entity == null) {
            return null;
        }
        ApplicationDependencyDTO dto = new ApplicationDependencyDTO();
        dto.id = entity.id;
        dto.applicationId = entity.applicationId;
        dto.environmentId = entity.environmentId;
        dto.type = entity.type;
        dto.dependencyName = entity.dependencyName;
        dto.dependencyVersion = entity.dependencyVersion;
        dto.description = entity.description;
        dto.active = entity.active;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;
        return dto;
    }

    public ApplicationDependency toEntity(ApplicationDependencyDTO dto) {
        if (dto == null) {
            return null;
        }
        ApplicationDependency entity = new ApplicationDependency();
        entity.id = dto.id;
        entity.applicationId = dto.applicationId;
        entity.environmentId = dto.environmentId;
        entity.type = dto.type;
        entity.dependencyName = dto.dependencyName;
        entity.dependencyVersion = dto.dependencyVersion;
        entity.description = dto.description;
        entity.active = dto.active != null ? dto.active : true;
        entity.createdAt = dto.createdAt;
        entity.updatedAt = dto.updatedAt;
        return entity;
    }

    public void updateEntity(ApplicationDependency entity, ApplicationDependencyDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.environmentId != null) {
            entity.environmentId = dto.environmentId;
        }
        if (dto.type != null) {
            entity.type = dto.type;
        }
        if (dto.dependencyName != null) {
            entity.dependencyName = dto.dependencyName;
        }
        if (dto.dependencyVersion != null) {
            entity.dependencyVersion = dto.dependencyVersion;
        }
        if (dto.description != null) {
            entity.description = dto.description;
        }
        if (dto.active != null) {
            entity.active = dto.active;
        }
    }
}