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
}