package com.easystation.agent.memory.repository;

import com.easystation.agent.memory.domain.Session;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话 Repository
 */
@ApplicationScoped
public class SessionRepository implements PanacheRepository<Session> {

    /**
     * 按会话 ID 查找
     */
    public Session findBySessionId(String sessionId) {
        return find("sessionId", sessionId).firstResult();
    }

    /**
     * 按 Agent 查找会话
     */
    public List<Session> findByAgentId(String agentId) {
        return find("agentId", agentId).list();
    }

    /**
     * 查找不活跃会话
     */
    public List<Session> findInactive(LocalDateTime threshold) {
        return find("lastAccessedAt < ?1", threshold).list();
    }

    /**
     * 查找用户的会话
     */
    public List<Session> findByUserId(String userId) {
        return find("userId", userId).list();
    }

    /**
     * 删除过期会话
     */
    public long cleanupInactive(LocalDateTime threshold) {
        return delete("lastAccessedAt < ?1", threshold);
    }
}
