package com.easystation.agent.memory.repository;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 记忆 Repository
 */
@ApplicationScoped
public class MemoryRepository implements PanacheRepository<Memory> {

    /**
     * 按会话查找记忆
     */
    public List<Memory> findBySessionId(String sessionId) {
        return find("sessionId", sessionId).list();
    }

    /**
     * 按会话和类型查找记忆（带限制）
     */
    public List<Memory> findBySessionIdAndType(String sessionId, MemoryType type, int limit) {
        return find("sessionId = ?1 and type = ?2", sessionId, type)
                .page(0, limit)
                .list();
    }

    /**
     * 查找过期记忆
     */
    public List<Memory> findExpired(LocalDateTime now) {
        return find("expiresAt < ?1", now).list();
    }

    /**
     * 按重要性查找记忆
     */
    public List<Memory> findByImportance(MemoryType type, int limit) {
        return find("type = ?1 order by importance desc, accessCount desc", type)
                .page(0, limit)
                .list();
    }

    /**
     * 删除会话的所有记忆
     */
    public void deleteBySessionId(String sessionId) {
        delete("sessionId", sessionId);
    }

    /**
     * 清理过期记忆
     */
    public long cleanupExpired() {
        return delete("expiresAt < ?1", LocalDateTime.now());
    }

    /**
     * 查找压缩候选（访问少、重要性低）
     */
    public List<Memory> findCompressionCandidates(MemoryType type, int minAgeHours, int limit) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(minAgeHours);
        return find("type = ?1 and createdAt < ?2 and accessCount < 3 and importance = ?3", 
                    type, threshold, com.easystation.agent.memory.domain.MemoryImportance.LOW)
                .page(0, limit)
                .list();
    }
}
