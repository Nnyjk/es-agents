package com.easystation.deployment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署历史查询请求 DTO
 */
@Data
public class DeploymentHistoryQueryDTO {
    public int pageNum = 1;
    public int pageSize = 10;
    public String sortBy = "createdAt";
    public String sortOrder = "DESC";
    public UUID applicationId;
    public UUID environmentId;
    public String version;
    public String status;
    public String triggeredBy;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String keyword;
}