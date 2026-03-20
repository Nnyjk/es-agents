package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.DeploymentHistory;
import com.easystation.agent.domain.enums.DeploymentStatus;
import com.easystation.agent.record.DeploymentHistoryRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class DeploymentHistoryService {

    /**
     * Get all deployment history for an agent instance.
     */
    public List<DeploymentHistoryRecord.ListResponse> getHistoryByAgentInstance(UUID agentInstanceId) {
        // Verify agent instance exists
        AgentInstance instance = AgentInstance.findById(agentInstanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        List<DeploymentHistory> history = DeploymentHistory.findByAgentInstanceId(agentInstanceId);
        return history.stream()
                .map(DeploymentHistoryRecord.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get deployment history detail by ID.
     */
    public DeploymentHistoryRecord.DetailResponse getById(UUID id) {
        DeploymentHistory history = DeploymentHistory.findById(id);
        if (history == null) {
            throw new WebApplicationException("Deployment history not found", Response.Status.NOT_FOUND);
        }
        return DeploymentHistoryRecord.DetailResponse.from(history);
    }

    /**
     * Create a new deployment history record.
     */
    @Transactional
    public DeploymentHistoryRecord.DetailResponse create(DeploymentHistoryRecord.CreateRequest request, String username) {
        AgentInstance instance = AgentInstance.findById(request.agentInstanceId());
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        DeploymentHistory history = new DeploymentHistory();
        history.agentInstance = instance;
        history.version = request.version();
        history.config = request.config();
        history.description = request.description();
        history.status = DeploymentStatus.PENDING;
        history.createdBy = username;
        history.startedAt = LocalDateTime.now();
        history.persist();

        Log.infof("Created deployment history %s for agent instance %s", history.id, instance.id);
        return DeploymentHistoryRecord.DetailResponse.from(history);
    }

    /**
     * Update deployment status.
     */
    @Transactional
    public DeploymentHistoryRecord.DetailResponse updateStatus(UUID id, DeploymentStatus status) {
        DeploymentHistory history = DeploymentHistory.findById(id);
        if (history == null) {
            throw new WebApplicationException("Deployment history not found", Response.Status.NOT_FOUND);
        }

        history.status = status;
        if (status == DeploymentStatus.SUCCESS || status == DeploymentStatus.FAILED) {
            history.finishedAt = LocalDateTime.now();
        }
        history.persist();

        Log.infof("Updated deployment history %s status to %s", history.id, status);
        return DeploymentHistoryRecord.DetailResponse.from(history);
    }

    /**
     * Rollback to a specific deployment.
     * Creates a new deployment history record that references the original.
     */
    @Transactional
    public DeploymentHistoryRecord.RollbackResponse rollback(UUID id, String reason, String username) {
        DeploymentHistory original = DeploymentHistory.findById(id);
        if (original == null) {
            throw new WebApplicationException("Deployment history not found", Response.Status.NOT_FOUND);
        }

        if (original.status != DeploymentStatus.SUCCESS) {
            throw new WebApplicationException("Can only rollback from a successful deployment", Response.Status.BAD_REQUEST);
        }

        // Mark original as rolled back
        original.status = DeploymentStatus.ROLLED_BACK;
        original.rollbackBy = username;
        original.rollbackAt = LocalDateTime.now();
        original.persist();

        // Create new deployment history for the rollback
        DeploymentHistory rollback = new DeploymentHistory();
        rollback.agentInstance = original.agentInstance;
        rollback.version = original.version;
        rollback.config = original.config;
        rollback.description = "Rollback: " + (reason != null ? reason : "No reason provided");
        rollback.status = DeploymentStatus.SUCCESS;
        rollback.rollbackFrom = original.id;
        rollback.createdBy = username;
        rollback.startedAt = LocalDateTime.now();
        rollback.finishedAt = LocalDateTime.now();
        rollback.persist();

        Log.infof("Rolled back deployment %s to create new deployment %s", original.id, rollback.id);
        return new DeploymentHistoryRecord.RollbackResponse(
                rollback.id,
                original.id,
                "Rollback completed successfully"
        );
    }

    /**
     * Delete deployment history.
     */
    @Transactional
    public void delete(UUID id) {
        DeploymentHistory history = DeploymentHistory.findById(id);
        if (history == null) {
            throw new WebApplicationException("Deployment history not found", Response.Status.NOT_FOUND);
        }

        history.delete();
        Log.infof("Deleted deployment history %s", id);
    }

    /**
     * Get the latest successful deployment for an agent instance.
     */
    public DeploymentHistoryRecord.DetailResponse getLatestSuccessful(UUID agentInstanceId) {
        DeploymentHistory history = DeploymentHistory.findLatestSuccessful(agentInstanceId);
        if (history == null) {
            throw new WebApplicationException("No successful deployment found", Response.Status.NOT_FOUND);
        }
        return DeploymentHistoryRecord.DetailResponse.from(history);
    }
}