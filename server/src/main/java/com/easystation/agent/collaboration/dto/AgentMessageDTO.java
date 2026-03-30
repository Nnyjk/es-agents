package com.easystation.agent.collaboration.dto;

import com.easystation.agent.collaboration.domain.AgentMessage;
import com.easystation.agent.collaboration.domain.MessageType;
import java.time.LocalDateTime;

public class AgentMessageDTO {
    public Long id;
    public Long sessionId;
    public MessageType type;
    public String fromAgentId;
    public String toAgentId;
    public Long correlationId;
    public String subject;
    public String content;
    public String metadata;
    public LocalDateTime createdAt;

    /**
     * 从实体转换为 DTO
     */
    public static AgentMessageDTO fromEntity(AgentMessage message) {
        AgentMessageDTO dto = new AgentMessageDTO();
        dto.id = message.id;
        dto.sessionId = message.sessionId;
        dto.type = message.type;
        dto.fromAgentId = message.fromAgentId;
        dto.toAgentId = message.toAgentId;
        dto.correlationId = message.correlationId;
        dto.subject = message.subject;
        dto.content = message.content;
        dto.metadata = message.metadata;
        dto.createdAt = message.createdAt;
        return dto;
    }
}
