package com.easystation.deployment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StageExecutionDTO {
    public String name;
    public String status;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String logs;
}