package com.easystation.export.dto;

import java.util.List;

public record ExportTaskListResponse(
    List<ExportTaskDTO> tasks,
    long total,
    int pendingCount,
    int processingCount,
    int completedCount,
    int failedCount
) {}