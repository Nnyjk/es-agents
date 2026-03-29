package com.easystation.agent.memory.dto;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 记忆 DTO
 */
public class MemoryDTO {
    public UUID id;
    public String sessionId;
    public MemoryType type;
    public String content;
    public Map<String, String> metadata;
    public LocalDateTime createdAt;
    public LocalDateTime expiresAt;
    public int accessCount;
    public String importance;
    public boolean compressed;

    public static MemoryDTO fromMemory(Memory memory) {
        MemoryDTO dto = new MemoryDTO();
        dto.id = memory.id;
        dto.sessionId = memory.sessionId;
        dto.type = memory.type;
        dto.content = memory.content;
        dto.metadata = memory.metadata != null ? new HashMap<>(memory.metadata) : new HashMap<>();
        dto.createdAt = memory.createdAt;
        dto.expiresAt = memory.expiresAt;
        dto.accessCount = memory.accessCount;
        dto.importance = memory.importance != null ? memory.importance.name() : null;
        dto.compressed = memory.compressed;
        return dto;
    }
}
