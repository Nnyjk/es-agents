package com.easystation.agent.record;

import com.easystation.agent.domain.DeploymentHistory;
import com.easystation.agent.domain.enums.DeploymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeploymentHistoryRecord {

    public record ListResponse(
            UUID id,
            UUID agentInstanceId,
            String hostName,
            String version,
            DeploymentStatus status,
            String description,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt,
            String createdBy,
            boolean canRollback
    ) {
        public static ListResponse from(DeploymentHistory history) {
            return new ListResponse(
                    history.id,
                    history.agentInstance != null ? history.agentInstance.id : null,
                    history.agentInstance != null && history.agentInstance.host != null 
                            ? history.agentInstance.host.name : null,
                    history.version,
                    history.status,
                    history.description,
                    history.startedAt,
                    history.finishedAt,
                    history.createdAt,
                    history.createdBy,
                    history.status == DeploymentStatus.SUCCESS
            );
        }
    }

    public record DetailResponse(
            UUID id,
            UUID agentInstanceId,
            String hostName,
            String version,
            DeploymentStatus status,
            String config,
            String description,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            UUID rollbackFrom,
            String rollbackBy,
            LocalDateTime rollbackAt,
            LocalDateTime createdAt,
            String createdBy
    ) {
        public static DetailResponse from(DeploymentHistory history) {
            return new DetailResponse(
                    history.id,
                    history.agentInstance != null ? history.agentInstance.id : null,
                    history.agentInstance != null && history.agentInstance.host != null 
                            ? history.agentInstance.host.name : null,
                    history.version,
                    history.status,
                    history.config,
                    history.description,
                    history.startedAt,
                    history.finishedAt,
                    history.rollbackFrom,
                    history.rollbackBy,
                    history.rollbackAt,
                    history.createdAt,
                    history.createdBy
            );
        }
    }

    public record CreateRequest(
            UUID agentInstanceId,
            String version,
            String config,
            String description
    ) {}

    public record RollbackRequest(
            String reason
    ) {}

    public record RollbackResponse(
            UUID deploymentId,
            UUID rolledBackFrom,
            String message
    ) {}
}