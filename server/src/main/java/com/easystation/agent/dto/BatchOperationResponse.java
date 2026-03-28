package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.BatchOperationStatus;
import com.easystation.agent.domain.enums.OperationType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for batch operation.
 */
public record BatchOperationResponse(
    UUID id,
    OperationType operationType,
    BatchOperationStatus status,
    UUID operatorId,
    LocalDateTime createdAt,
    LocalDateTime completedAt,
    Integer totalItems,
    Integer successCount,
    Integer failedCount
) {}