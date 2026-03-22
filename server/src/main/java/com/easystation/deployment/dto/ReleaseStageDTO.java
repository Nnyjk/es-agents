package com.easystation.deployment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReleaseStageDTO {
    public String name;
    public String status;
    public Integer progress;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String logs;
    public String error;
}