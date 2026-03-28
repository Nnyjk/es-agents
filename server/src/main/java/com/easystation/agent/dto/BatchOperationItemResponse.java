package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.BatchOperationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for batch operation item.
 */
public record BatchOperationItemResponse(
    UUID id,
    UUID targetId,
    String targetType,
    BatchOperationStatus status,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {}