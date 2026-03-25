package com.easystation.deployment.mapper;

import com.easystation.deployment.domain.DeploymentRollback;
import com.easystation.deployment.dto.DeploymentRollbackDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部署回滚 Mapper
 */
public class DeploymentRollbackMapper {

    public static DeploymentRollbackDTO toDTO(DeploymentRollback entity) {
        if (entity == null) {
            return null;
        }
        DeploymentRollbackDTO dto = new DeploymentRollbackDTO();
        dto.setId(entity.getId());
        dto.setRollbackId(entity.getRollbackId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setEnvironmentId(entity.getEnvironmentId());
        dto.setFromVersionId(entity.getFromVersionId());
        dto.setToVersionId(entity.getToVersionId());
        dto.setFromVersion(entity.getFromVersion());
        dto.setToVersion(entity.getToVersion());
        dto.setStrategy(entity.getStrategy());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());
        dto.setPrecheckResult(entity.getPrecheckResult());
        dto.setPrecheckAt(entity.getPrecheckAt());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setDuration(entity.getDuration());
        dto.setLogs(entity.getLogs());
        dto.setVerifyResult(entity.getVerifyResult());
        dto.setVerifyPassed(entity.getVerifyPassed());
        dto.setNotifyConfig(entity.getNotifyConfig());
        dto.setTimeoutConfig(entity.getTimeoutConfig());
        dto.setRetryCount(entity.getRetryCount());
        dto.setMaxRetry(entity.getMaxRetry());
        dto.setTriggeredBy(entity.getTriggeredBy());
        dto.setTriggeredAt(entity.getTriggeredAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public static List<DeploymentRollbackDTO> toDTOList(List<DeploymentRollback> entities) {
        return entities.stream()
                .map(DeploymentRollbackMapper::toDTO)
                .collect(Collectors.toList());
    }
}