package com.easystation.export.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExportTaskDTO(
    UUID id,
    String exportType,
    String dataType,
    String status,
    String fileName,
    Integer totalRecords,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {}