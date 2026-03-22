package com.easystation.agent.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgentLogRecord(
    UUID agentId,
    String agentName,
    int totalCount,
    List<LogEntry> logs
) {
    public record LogEntry(
        int lineNumber,
        LocalDateTime timestamp,
        String level,
        String message
    ) {}

    public record Query(
        UUID agentId,
        Integer limit,
        Integer offset,
        String level,
        String keyword,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {}
    
    public record Stats(
        int totalCount,
        int errorCount,
        int warnCount,
        int infoCount,
        int debugCount
    ) {}

    /**
     * 部署日志查询参数
     */
    public record DeploymentLogQuery(
        UUID agentId,
        UUID deploymentId,
        Integer limit,
        Integer offset,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {}

    /**
     * 命令执行日志查询参数
     */
    public record CommandLogQuery(
        UUID agentId,
        UUID executionId,
        Integer limit,
        Integer offset,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {}

    /**
     * 任务执行结果记录
     */
    public record TaskLogRecord(
        UUID taskId,
        String taskType,
        String taskName,
        String status,
        Integer exitCode,
        Long durationMs,
        String output,
        String error,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {}
}