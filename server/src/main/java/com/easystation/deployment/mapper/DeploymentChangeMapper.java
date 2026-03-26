package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.DeploymentChangeRecord;
import com.easystation.deployment.dto.DeploymentChangeDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部署变更 Mapper
 */
public class DeploymentChangeMapper {

    public static DeploymentChangeDTO toDTO(DeploymentChangeRecord entity) {
        if (entity == null) {
            return null;
        }
        DeploymentChangeDTO dto = new DeploymentChangeDTO();
        dto.setId(entity.getId());
        dto.setVersionId(entity.getVersionId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setChangeType(entity.getChangeType());
        dto.setChangeKey(entity.getChangeKey());
        dto.setChangeTitle(entity.getChangeTitle());
        dto.setOldValue(entity.getOldValue());
        dto.setNewValue(entity.getNewValue());
        dto.setChangeDiff(entity.getChangeDiff());
        dto.setDescription(entity.getDescription());
        dto.setImpactLevel(entity.getImpactLevel());
        dto.setImpactScope(entity.getImpactScope());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setRiskDescription(entity.getRiskDescription());
        dto.setCommitHash(entity.getCommitHash());
        dto.setCommitUrl(entity.getCommitUrl());
        dto.setAuthor(entity.getAuthor());
        dto.setAuthorEmail(entity.getAuthorEmail());
        dto.setRelatedIssue(entity.getRelatedIssue());
        dto.setRelatedPr(entity.getRelatedPr());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    public static List<DeploymentChangeDTO> toDTOList(List<DeploymentChangeRecord> entities) {
        return entities.stream()
                .map(DeploymentChangeMapper::toDTO)
                .collect(Collectors.toList());
    }
}