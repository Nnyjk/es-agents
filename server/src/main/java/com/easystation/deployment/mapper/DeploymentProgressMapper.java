package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.DeploymentProgress;
import com.easystation.deployment.domain.DeploymentProgressHistory;
import com.easystation.deployment.dto.DeploymentProgressDTO;
import com.easystation.deployment.dto.DeploymentProgressHistoryDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部署进展 Mapper
 */
public class DeploymentProgressMapper {

    /**
     * 实体转 DTO
     */
    public static DeploymentProgressDTO toDTO(DeploymentProgress entity) {
        if (entity == null) {
            return null;
        }
        DeploymentProgressDTO dto = new DeploymentProgressDTO();
        dto.id = entity.id;
        dto.deploymentId = entity.deploymentId;
        dto.stage = entity.stage;
        dto.status = entity.status;
        dto.progressPercent = entity.progressPercent;
        dto.message = entity.message;
        dto.startedAt = entity.startedAt;
        dto.completedAt = entity.completedAt;
        dto.createdAt = entity.createdAt;
        dto.updatedAt = entity.updatedAt;
        return dto;
    }

    /**
     * 实体列表转 DTO 列表
     */
    public static List<DeploymentProgressDTO> toDTOList(List<DeploymentProgress> entities) {
        return entities.stream()
            .map(DeploymentProgressMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * 历史实体转 DTO
     */
    public static DeploymentProgressHistoryDTO toHistoryDTO(DeploymentProgressHistory entity) {
        if (entity == null) {
            return null;
        }
        DeploymentProgressHistoryDTO dto = new DeploymentProgressHistoryDTO();
        dto.id = entity.id;
        dto.deploymentId = entity.deploymentId;
        dto.stage = entity.stage;
        dto.oldStatus = entity.oldStatus;
        dto.newStatus = entity.newStatus;
        dto.message = entity.message;
        dto.createdAt = entity.createdAt;
        return dto;
    }

    /**
     * 历史实体列表转 DTO 列表
     */
    public static List<DeploymentProgressHistoryDTO> toHistoryDTOList(List<DeploymentProgressHistory> entities) {
        return entities.stream()
            .map(DeploymentProgressMapper::toHistoryDTO)
            .collect(Collectors.toList());
    }
}