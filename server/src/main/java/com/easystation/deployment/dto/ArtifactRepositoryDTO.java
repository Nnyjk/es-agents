package com.easystation.deployment.dto;

import com.easystation.deployment.domain.ArtifactRepository;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 制品仓库 DTO
 */
@Data
public class ArtifactRepositoryDTO {
    public UUID id;
    public String name;
    public ArtifactRepository.RepositoryType type;
    public String url;
    public UUID credentialId;
    public String description;
    public Boolean active;
    public Boolean isDefault;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}