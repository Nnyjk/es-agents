package com.easystation.export.dto;

import java.util.List;
import java.util.UUID;

public record ExportResponse(
    UUID taskId,
    String status,
    String message
) {}