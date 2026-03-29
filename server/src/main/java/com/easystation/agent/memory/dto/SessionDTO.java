package com.easystation.agent.memory.dto;

import com.easystation.agent.memory.domain.Session;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话 DTO
 */
public class SessionDTO {
    public UUID id;
    public String sessionId;
    public String agentId;
    public String userId;
    public String title;
    public Map<String, String> context;
    public LocalDateTime createdAt;
    public LocalDateTime lastAccessedAt;
    public int messageCount;
    public boolean archived;

    public static SessionDTO fromSession(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.id = session.id;
        dto.sessionId = session.sessionId;
        dto.agentId = session.agentId;
        dto.userId = session.userId;
        dto.title = session.title;
        dto.context = session.context != null ? new HashMap<>(session.context) : new HashMap<>();
        dto.createdAt = session.createdAt;
        dto.lastAccessedAt = session.lastAccessedAt;
        dto.messageCount = session.messageCount;
        dto.archived = session.archived;
        return dto;
    }
}
