package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.ArtifactRepository;
import com.easystation.deployment.dto.ArtifactRepositoryDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * ArtifactRepository DTO 转换器
 */
@ApplicationScoped
public class ArtifactRepositoryMapper {

    public ArtifactRepositoryDTO toDTO(ArtifactRepository entity) {
        if (entity == null) {
            return null;
        }
        ArtifactRepositoryDTO dto = new ArtifactRepositoryDTO();
        dto.id = entity.id;
        dto.name = entity.name;
        dto.type = entity.type;
        dto.url = entity.url;
        dto.credentialId = entity.credentialId;
        dto.description = entity.description;
        dto.active = entity.active;
        dto.isDefault = entity.isDefault;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;
        return dto;
    }

    public ArtifactRepository toEntity(ArtifactRepositoryDTO dto) {
        if (dto == null) {
            return null;
        }
        ArtifactRepository entity = new ArtifactRepository();
        entity.id = dto.id;
        entity.name = dto.name;
        entity.type = dto.type;
        entity.url = dto.url;
        entity.credentialId = dto.credentialId;
        entity.description = dto.description;
        entity.active = dto.active != null ? dto.active : true;
        entity.isDefault = dto.isDefault != null ? dto.isDefault : false;
        entity.createdAt = dto.createdAt;
        entity.updatedAt = dto.updatedAt;
        return entity;
    }

    public void updateEntity(ArtifactRepository entity, ArtifactRepositoryDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.name != null) {
            entity.name = dto.name;
        }
        if (dto.type != null) {
            entity.type = dto.type;
        }
        if (dto.url != null) {
            entity.url = dto.url;
        }
        if (dto.credentialId != null) {
            entity.credentialId = dto.credentialId;
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