package com.easystation.agent.record;

import java.util.UUID;

public record AgentTaskRecord(
    UUID id,
    String commandName,
    String script,
    String args,
    Long timeout
) {}
