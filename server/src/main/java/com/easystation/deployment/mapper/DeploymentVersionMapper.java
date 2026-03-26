package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.DeploymentVersion;
import com.easystation.deployment.dto.DeploymentVersionDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部署版本 Mapper
 */
public class DeploymentVersionMapper {

    public static DeploymentVersionDTO toDTO(DeploymentVersion entity) {
        if (entity == null) {
            return null;
        }
        DeploymentVersionDTO dto = new DeploymentVersionDTO();
        dto.setId(entity.getId());
        dto.setVersionId(entity.getVersionId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setEnvironmentId(entity.getEnvironmentId());
        dto.setReleaseId(entity.getReleaseId());
        dto.setVersion(entity.getVersion());
        dto.setCommitHash(entity.getCommitHash());
        dto.setCommitMessage(entity.getCommitMessage());
        dto.setCommitAuthor(entity.getCommitAuthor());
        dto.setCommitTime(entity.getCommitTime());
        dto.setBuildNumber(entity.getBuildNumber());
        dto.setBuildUrl(entity.getBuildUrl());
        dto.setArtifactUrl(entity.getArtifactUrl());
        dto.setArtifactChecksum(entity.getArtifactChecksum());
        dto.setStatus(entity.getStatus());
        dto.setConfig(entity.getConfig());
        dto.setDeployConfig(entity.getDeployConfig());
        dto.setDeployBy(entity.getDeployBy());
        dto.setDeployAt(entity.getDeployAt());
        dto.setDeployDuration(entity.getDeployDuration());
        dto.setRollbackBy(entity.getRollbackBy());
        dto.setRollbackAt(entity.getRollbackAt());
        dto.setNotes(entity.getNotes());
        dto.setIsStable(entity.getIsStable());
        dto.setIsProblematic(entity.getIsProblematic());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public static List<DeploymentVersionDTO> toDTOList(List<DeploymentVersion> entities) {
        return entities.stream()
                .map(DeploymentVersionMapper::toDTO)
                .collect(Collectors.toList());
    }
}