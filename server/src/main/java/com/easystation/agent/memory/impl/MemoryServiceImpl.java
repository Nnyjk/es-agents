package com.easystation.agent.memory.impl;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryImportance;
import com.easystation.agent.memory.domain.MemoryType;
import com.easystation.agent.memory.domain.Session;
import com.easystation.agent.memory.repository.MemoryRepository;
import com.easystation.agent.memory.repository.SessionRepository;
import com.easystation.agent.memory.spi.MemoryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 记忆服务实现
 */
@ApplicationScoped
public class MemoryServiceImpl implements MemoryService {

    @Inject
    MemoryRepository memoryRepository;

    @Inject
    SessionRepository sessionRepository;

    private static final int SHORT_TERM_EXPIRY_HOURS = 24;
    private static final int SHORT_TERM_LIMIT = 50;
    private static final int LONG_TERM_LIMIT = 100;

    // ==================== 会话管理 ====================

    @Override
    @Transactional
    public Session createSession(String agentId, String userId) {
        Session session = new Session();
        session.sessionId = UUID.randomUUID().toString();
        session.agentId = agentId;
        session.userId = userId;
        session.context = new HashMap<>();
        sessionRepository.persist(session);
        return session;
    }

    @Override
    public Session getSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            session.recordMessage();
        }
        return session;
    }

    @Override
    @Transactional
    public void touchSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            session.lastAccessedAt = LocalDateTime.now();
        }
    }

    @Override
    @Transactional
    public void archiveSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            session.archived = true;
        }
    }

    // ==================== 短期记忆 ====================

    @Override
    @Transactional
    public void addShortTermMemory(String sessionId, String content) {
        Session session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        Memory memory = new Memory();
        memory.sessionId = sessionId;
        memory.type = MemoryType.SHORT_TERM;
        memory.content = content;
        memory.expiresAt = LocalDateTime.now().plusHours(SHORT_TERM_EXPIRY_HOURS);
        memory.importance = MemoryImportance.MEDIUM;
        memoryRepository.persist(memory);

        // 清理过期记忆
        cleanupExpiredShortTermMemories();
    }

    @Override
    public List<Memory> getShortTermMemories(String sessionId, int limit) {
        return memoryRepository.findBySessionIdAndType(sessionId, MemoryType.SHORT_TERM, limit)
                .stream()
                .peek(Memory::recordAccess)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cleanupExpiredShortTermMemories() {
        List<Memory> expired = memoryRepository.findExpired(LocalDateTime.now());
        for (Memory memory : expired) {
            if (memory.type == MemoryType.SHORT_TERM) {
                memoryRepository.delete(memory);
            }
        }
    }

    // ==================== 长期记忆 ====================

    @Override
    @Transactional
    public void addLongTermMemory(String sessionId, String content, Map<String, Object> metadata, MemoryImportance importance) {
        Session session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        Memory memory = new Memory();
        memory.sessionId = sessionId;
        memory.type = MemoryType.LONG_TERM;
        memory.content = content;
        memory.importance = importance != null ? importance : MemoryImportance.MEDIUM;
        
        if (metadata != null) {
            memory.metadata = metadata.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue() != null ? e.getValue().toString() : ""
                    ));
        }
        
        memoryRepository.persist(memory);
    }

    @Override
    public List<Memory> searchLongTermMemories(String sessionId, String query, int limit) {
        List<Memory> memories = memoryRepository.findBySessionIdAndType(sessionId, MemoryType.LONG_TERM, limit * 2);
        
        // 简单关键词匹配（后续可升级为向量搜索）
        String lowerQuery = query.toLowerCase();
        return memories.stream()
                .filter(m -> m.content.toLowerCase().contains(lowerQuery))
                .peek(Memory::recordAccess)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Memory> getLongTermMemories(String sessionId, int limit) {
        return memoryRepository.findBySessionIdAndType(sessionId, MemoryType.LONG_TERM, limit)
                .stream()
                .peek(Memory::recordAccess)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLongTermMemory(String memoryId) {
        Memory memory = memoryRepository.findById(UUID.fromString(memoryId));
        if (memory != null && memory.type == MemoryType.LONG_TERM) {
            memoryRepository.delete(memory);
        }
    }

    // ==================== 上下文管理 ====================

    @Override
    @Transactional
    public void setContextVariable(String sessionId, String key, String value) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            session.setContextVariable(key, value);
        }
    }

    @Override
    public String getContextVariable(String sessionId, String key) {
        Session session = sessionRepository.findBySessionId(sessionId);
        return session != null ? session.getContextVariable(key) : null;
    }

    @Override
    public Map<String, String> getAllContextVariables(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId);
        return session != null ? new HashMap<>(session.context) : new HashMap<>();
    }

    // ==================== 记忆压缩 ====================

    @Override
    public String compressMemories(List<Memory> memories) {
        if (memories == null || memories.isEmpty()) {
            return "";
        }
        
        // 简单摘要：取每条记忆的前 100 字符
        StringBuilder summary = new StringBuilder();
        summary.append("[记忆摘要] ");
        for (Memory memory : memories) {
            String preview = memory.content.length() > 100 
                    ? memory.content.substring(0, 100) + "..." 
                    : memory.content;
            summary.append(preview).append(" | ");
        }
        return summary.toString();
    }

    @Override
    @Transactional
    public void autoCompressMemories(String sessionId) {
        List<Memory> candidates = memoryRepository.findCompressionCandidates(
                MemoryType.SHORT_TERM, 12, 20);
        
        if (candidates.size() >= 10) {
            String compressed = compressMemories(candidates);
            
            // 创建压缩后的记忆
            Memory compressedMemory = new Memory();
            compressedMemory.sessionId = sessionId;
            compressedMemory.type = MemoryType.LONG_TERM;
            compressedMemory.content = compressed;
            compressedMemory.compressed = true;
            compressedMemory.importance = MemoryImportance.LOW;
            memoryRepository.persist(compressedMemory);
            
            // 删除原始记忆
            for (Memory memory : candidates) {
                memoryRepository.delete(memory);
            }
        }
    }

    // ==================== 清理 ====================

    @Override
    @Transactional
    public void cleanupSession(String sessionId) {
        memoryRepository.deleteBySessionId(sessionId);
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session != null) {
            sessionRepository.delete(session);
        }
    }
}
