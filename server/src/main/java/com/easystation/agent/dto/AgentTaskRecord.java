package com.easystation.agent.dto;

import java.util.UUID;

public record AgentTaskRecord(
    UUID id,
    String commandName,
    String script,
    String args,
    Long timeout
) {}
