package com.easystation.agent.collaboration.dto;

import java.time.LocalDateTime;

public class CollaborationSessionDTO {
    public Long id;
    public String name;
    public String description;
    public String status;
    public String agentIds;
    public String creatorAgentId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime closedAt;
}
