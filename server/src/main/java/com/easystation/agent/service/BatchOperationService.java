package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.BatchOperation;
import com.easystation.agent.domain.BatchOperationItem;
import com.easystation.agent.domain.enums.BatchOperationStatus;
import com.easystation.agent.domain.enums.OperationType;
import com.easystation.infra.domain.Host;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BatchOperationService {

    @Inject
    ManagedExecutor managedExecutor;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Create a batch command execution task.
     */
    @Transactional
    public BatchOperation createBatchCommand(List<UUID> hostIds, String command, UUID operatorId) {
        if (hostIds == null || hostIds.isEmpty()) {
            throw new WebApplicationException("Host IDs cannot be empty", Response.Status.BAD_REQUEST);
        }
        if (command == null || command.isBlank()) {
            throw new WebApplicationException("Command cannot be empty", Response.Status.BAD_REQUEST);
        }

        BatchOperation operation = new BatchOperation();
        operation.operationType = OperationType.BATCH_COMMAND;
        operation.status = BatchOperationStatus.PENDING;
        operation.operatorId = operatorId;
        operation.totalItems = hostIds.size();
        operation.successCount = 0;
        operation.failedCount = 0;
        operation.persist();

        // Create items for each host
        for (UUID hostId : hostIds) {
            BatchOperationItem item = new BatchOperationItem();
            item.batchOperation = operation;
            item.targetId = hostId;
            item.targetType = "Host";
            item.status = BatchOperationStatus.PENDING;
            item.persist();
        }

        // Start async processing
        processBatchCommandAsync(operation.id, command);

        Log.infof("Created batch command operation %s with %d hosts", operation.id, hostIds.size());
        return operation;
    }

    /**
     * Create a batch deploy task.
     */
    @Transactional
    public BatchOperation createBatchDeploy(List<UUID> agentIds, UUID operatorId) {
        if (agentIds == null || agentIds.isEmpty()) {
            throw new WebApplicationException("Agent IDs cannot be empty", Response.Status.BAD_REQUEST);
        }

        BatchOperation operation = new BatchOperation();
        operation.operationType = OperationType.BATCH_DEPLOY;
        operation.status = BatchOperationStatus.PENDING;
        operation.operatorId = operatorId;
        operation.totalItems = agentIds.size();
        operation.successCount = 0;
        operation.failedCount = 0;
        operation.persist();

        // Create items for each agent
        for (UUID agentId : agentIds) {
            BatchOperationItem item = new BatchOperationItem();
            item.batchOperation = operation;
            item.targetId = agentId;
            item.targetType = "AgentInstance";
            item.status = BatchOperationStatus.PENDING;
            item.persist();
        }

        // Start async processing
        processBatchDeployAsync(operation.id);

        Log.infof("Created batch deploy operation %s with %d agents", operation.id, agentIds.size());
        return operation;
    }

    /**
     * Create a batch upgrade task.
     */
    @Transactional
    public BatchOperation createBatchUpgrade(List<UUID> agentIds, String version, UUID operatorId) {
        if (agentIds == null || agentIds.isEmpty()) {
            throw new WebApplicationException("Agent IDs cannot be empty", Response.Status.BAD_REQUEST);
        }
        if (version == null || version.isBlank()) {
            throw new WebApplicationException("Version cannot be empty", Response.Status.BAD_REQUEST);
        }

        BatchOperation operation = new BatchOperation();
        operation.operationType = OperationType.BATCH_UPGRADE;
        operation.status = BatchOperationStatus.PENDING;
        operation.operatorId = operatorId;
        operation.totalItems = agentIds.size();
        operation.successCount = 0;
        operation.failedCount = 0;
        operation.persist();

        // Create items for each agent
        for (UUID agentId : agentIds) {
            BatchOperationItem item = new BatchOperationItem();
            item.batchOperation = operation;
            item.targetId = agentId;
            item.targetType = "AgentInstance";
            item.status = BatchOperationStatus.PENDING;
            item.persist();
        }

        // Start async processing
        processBatchUpgradeAsync(operation.id, version);

        Log.infof("Created batch upgrade operation %s with %d agents to version %s",
                operation.id, agentIds.size(), version);
        return operation;
    }

    /**
     * Get batch operation details by ID.
     */
    public BatchOperation getBatchOperation(UUID id) {
        BatchOperation operation = BatchOperation.findById(id);
        if (operation == null) {
            throw new WebApplicationException("Batch operation not found", Response.Status.NOT_FOUND);
        }
        return operation;
    }

    /**
     * Get batch operation items.
     */
    public List<BatchOperationItem> getBatchOperationItems(UUID operationId) {
        return BatchOperationItem.findByBatchOperationId(operationId);
    }

    /**
     * List batch operations with pagination.
     */
    public List<BatchOperation> listBatchOperations(int page, int size) {
        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        if (pageSize <= 0) pageSize = DEFAULT_PAGE_SIZE;
        int pageIndex = Math.max(page, 0);

        return BatchOperation.find("order by createdAt desc")
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    /**
     * Count total batch operations.
     */
    public long countBatchOperations() {
        return BatchOperation.count();
    }

    /**
     * Update item status and recalculate parent operation status.
     */
    @Transactional
    public void updateItemStatus(UUID itemId, BatchOperationStatus status, String errorMessage) {
        BatchOperationItem item = BatchOperationItem.findById(itemId);
        if (item == null) {
            Log.warnf("Batch operation item not found: %s", itemId);
            return;
        }

        item.status = status;
        item.errorMessage = errorMessage;

        if (status == BatchOperationStatus.RUNNING && item.startedAt == null) {
            item.startedAt = LocalDateTime.now();
        }

        if (isTerminalStatus(status) && item.completedAt == null) {
            item.completedAt = LocalDateTime.now();
        }

        item.persist();

        // Recalculate parent operation status
        checkBatchOperationCompletion(item.batchOperation.id);
    }

    /**
     * Check if batch operation is complete and update status.
     */
    @Transactional
    public BatchOperationStatus checkBatchOperationCompletion(UUID operationId) {
        BatchOperation operation = BatchOperation.findById(operationId);
        if (operation == null) {
            Log.warnf("Batch operation not found: %s", operationId);
            return null;
        }

        // Count items by status
        long pendingCount = BatchOperationItem.countByBatchOperationIdAndStatus(operationId, BatchOperationStatus.PENDING);
        long runningCount = BatchOperationItem.countByBatchOperationIdAndStatus(operationId, BatchOperationStatus.RUNNING);
        long successCount = BatchOperationItem.countByBatchOperationIdAndStatus(operationId, BatchOperationStatus.SUCCESS);
        long failedCount = BatchOperationItem.countByBatchOperationIdAndStatus(operationId, BatchOperationStatus.FAILED);

        // Update counts
        operation.successCount = (int) successCount;
        operation.failedCount = (int) failedCount;

        // Determine if all items are complete
        if (pendingCount == 0 && runningCount == 0) {
            // All items are terminal
            if (failedCount == 0) {
                operation.status = BatchOperationStatus.SUCCESS;
            } else if (successCount == 0) {
                operation.status = BatchOperationStatus.FAILED;
            } else {
                operation.status = BatchOperationStatus.PARTIAL_SUCCESS;
            }
            operation.completedAt = LocalDateTime.now();
            Log.infof("Batch operation %s completed with status=%s, success=%d, failed=%d",
                    operationId, operation.status, successCount, failedCount);
        } else if (runningCount > 0 && operation.status == BatchOperationStatus.PENDING) {
            // Some items are running
            operation.status = BatchOperationStatus.RUNNING;
        }

        operation.persist();
        return operation.status;
    }

    /**
     * Process batch command asynchronously.
     */
    private void processBatchCommandAsync(UUID operationId, String command) {
        managedExecutor.execute(() -> {
            try {
                processBatchCommand(operationId, command);
            } catch (Exception e) {
                Log.errorf(e, "Error processing batch command operation %s", operationId);
            }
        });
    }

    /**
     * Process batch command execution.
     */
    @Transactional
    void processBatchCommand(UUID operationId, String command) {
        BatchOperation operation = BatchOperation.findById(operationId);
        if (operation == null) {
            Log.warnf("Batch operation not found: %s", operationId);
            return;
        }

        // Update operation status to RUNNING
        operation.status = BatchOperationStatus.RUNNING;
        operation.persist();

        List<BatchOperationItem> items = BatchOperationItem.findByBatchOperationId(operationId);
        for (BatchOperationItem item : items) {
            try {
                // Update item status to RUNNING
                item.status = BatchOperationStatus.RUNNING;
                item.startedAt = LocalDateTime.now();
                item.persist();

                // Execute command on host
                Host host = Host.findById(item.targetId);
                if (host == null) {
                    throw new RuntimeException("Host not found: " + item.targetId);
                }

                // TODO: Implement actual command execution via WebSocket/SSH
                // For now, simulate success
                boolean success = executeCommandOnHost(host.id, command);

                if (success) {
                    item.status = BatchOperationStatus.SUCCESS;
                } else {
                    item.status = BatchOperationStatus.FAILED;
                    item.errorMessage = "Command execution failed";
                }
                item.completedAt = LocalDateTime.now();
                item.persist();

            } catch (Exception e) {
                Log.errorf(e, "Error executing command on host %s", item.targetId);
                item.status = BatchOperationStatus.FAILED;
                item.errorMessage = e.getMessage();
                item.completedAt = LocalDateTime.now();
                item.persist();
            }
        }

        // Check completion
        checkBatchOperationCompletion(operationId);
    }

    /**
     * Process batch deploy asynchronously.
     */
    private void processBatchDeployAsync(UUID operationId) {
        managedExecutor.execute(() -> {
            try {
                processBatchDeploy(operationId);
            } catch (Exception e) {
                Log.errorf(e, "Error processing batch deploy operation %s", operationId);
            }
        });
    }

    /**
     * Process batch deploy execution.
     */
    @Transactional
    void processBatchDeploy(UUID operationId) {
        BatchOperation operation = BatchOperation.findById(operationId);
        if (operation == null) {
            Log.warnf("Batch operation not found: %s", operationId);
            return;
        }

        // Update operation status to RUNNING
        operation.status = BatchOperationStatus.RUNNING;
        operation.persist();

        List<BatchOperationItem> items = BatchOperationItem.findByBatchOperationId(operationId);
        for (BatchOperationItem item : items) {
            try {
                // Update item status to RUNNING
                item.status = BatchOperationStatus.RUNNING;
                item.startedAt = LocalDateTime.now();
                item.persist();

                // Deploy agent instance
                AgentInstance instance = AgentInstance.findById(item.targetId);
                if (instance == null) {
                    throw new RuntimeException("Agent instance not found: " + item.targetId);
                }

                // TODO: Implement actual deploy logic
                // For now, simulate success
                boolean success = deployAgentInstance(instance.id);

                if (success) {
                    item.status = BatchOperationStatus.SUCCESS;
                } else {
                    item.status = BatchOperationStatus.FAILED;
                    item.errorMessage = "Deploy failed";
                }
                item.completedAt = LocalDateTime.now();
                item.persist();

            } catch (Exception e) {
                Log.errorf(e, "Error deploying agent %s", item.targetId);
                item.status = BatchOperationStatus.FAILED;
                item.errorMessage = e.getMessage();
                item.completedAt = LocalDateTime.now();
                item.persist();
            }
        }

        // Check completion
        checkBatchOperationCompletion(operationId);
    }

    /**
     * Process batch upgrade asynchronously.
     */
    private void processBatchUpgradeAsync(UUID operationId, String version) {
        managedExecutor.execute(() -> {
            try {
                processBatchUpgrade(operationId, version);
            } catch (Exception e) {
                Log.errorf(e, "Error processing batch upgrade operation %s", operationId);
            }
        });
    }

    /**
     * Process batch upgrade execution.
     */
    @Transactional
    void processBatchUpgrade(UUID operationId, String version) {
        BatchOperation operation = BatchOperation.findById(operationId);
        if (operation == null) {
            Log.warnf("Batch operation not found: %s", operationId);
            return;
        }

        // Update operation status to RUNNING
        operation.status = BatchOperationStatus.RUNNING;
        operation.persist();

        List<BatchOperationItem> items = BatchOperationItem.findByBatchOperationId(operationId);
        for (BatchOperationItem item : items) {
            try {
                // Update item status to RUNNING
                item.status = BatchOperationStatus.RUNNING;
                item.startedAt = LocalDateTime.now();
                item.persist();

                // Upgrade agent instance
                AgentInstance instance = AgentInstance.findById(item.targetId);
                if (instance == null) {
                    throw new RuntimeException("Agent instance not found: " + item.targetId);
                }

                // TODO: Implement actual upgrade logic
                // For now, simulate success
                boolean success = upgradeAgentInstance(instance.id, version);

                if (success) {
                    item.status = BatchOperationStatus.SUCCESS;
                } else {
                    item.status = BatchOperationStatus.FAILED;
                    item.errorMessage = "Upgrade failed";
                }
                item.completedAt = LocalDateTime.now();
                item.persist();

            } catch (Exception e) {
                Log.errorf(e, "Error upgrading agent %s", item.targetId);
                item.status = BatchOperationStatus.FAILED;
                item.errorMessage = e.getMessage();
                item.completedAt = LocalDateTime.now();
                item.persist();
            }
        }

        // Check completion
        checkBatchOperationCompletion(operationId);
    }

    /**
     * Execute command on host (placeholder for actual implementation).
     */
    private boolean executeCommandOnHost(UUID hostId, String command) {
        // TODO: Implement actual command execution via WebSocket/SSH
        Log.infof("Executing command on host %s: %s", hostId, command);
        return true; // Simulate success for now
    }

    /**
     * Deploy agent instance (placeholder for actual implementation).
     */
    private boolean deployAgentInstance(UUID agentId) {
        // TODO: Implement actual deploy logic
        Log.infof("Deploying agent instance %s", agentId);
        return true; // Simulate success for now
    }

    /**
     * Upgrade agent instance (placeholder for actual implementation).
     */
    private boolean upgradeAgentInstance(UUID agentId, String version) {
        // TODO: Implement actual upgrade logic
        Log.infof("Upgrading agent instance %s to version %s", agentId, version);
        return true; // Simulate success for now
    }

    /**
     * Check if status is terminal (not pending or running).
     */
    private boolean isTerminalStatus(BatchOperationStatus status) {
        return status == BatchOperationStatus.SUCCESS
                || status == BatchOperationStatus.FAILED
                || status == BatchOperationStatus.PARTIAL_SUCCESS;
    }
}