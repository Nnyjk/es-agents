package com.easystation.export.dto;

import java.time.LocalDateTime;

public record ExportRequest(
    String exportType,
    String dataType,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String keyword,
    String status,
    String action,
    String resourceType,
    Integer limit,
    Integer offset
) {}