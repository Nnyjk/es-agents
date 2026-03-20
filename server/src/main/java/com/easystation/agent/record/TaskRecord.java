package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentTaskStatus;

import java.util.UUID;

public class TaskRecord {

    public record ExecuteRequest(
            UUID agentInstanceId,
            UUID commandId,
            String args
    ) {}

    public record ExecuteScriptRequest(
            UUID agentInstanceId,
            String script,
            Long timeout
    ) {}

    public record RetryRequest(
            String reason
    ) {}

    public record TaskCounts(
            long pending,
            long sent,
            long running,
            long success,
            long failed
    ) {}
}