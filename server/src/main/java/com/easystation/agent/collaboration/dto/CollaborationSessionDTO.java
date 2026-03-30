package com.easystation.agent.collaboration.dto;

import com.easystation.agent.collaboration.domain.CollaborationSession;
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

    /**
     * 从实体转换为 DTO
     */
    public static CollaborationSessionDTO fromEntity(CollaborationSession session) {
        CollaborationSessionDTO dto = new CollaborationSessionDTO();
        dto.id = session.id;
        dto.name = session.name;
        dto.description = session.description;
        dto.status = session.status;
        dto.agentIds = session.agentIds;
        dto.creatorAgentId = session.creatorAgentId;
        dto.createdAt = session.createdAt;
        dto.updatedAt = session.updatedAt;
        dto.closedAt = session.closedAt;
        return dto;
    }
}
