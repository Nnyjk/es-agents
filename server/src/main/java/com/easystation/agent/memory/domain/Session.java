package com.easystation.agent.memory.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话实体
 * 管理 Agent 对话会话和上下文
 */
@Entity
@Table(name = "agent_session", 
       uniqueConstraints = @UniqueConstraint(columnNames = "sessionId"))
@NamedQuery(name = "Session.findBySessionId", query = "SELECT s FROM Session s WHERE s.sessionId = :sessionId")
@NamedQuery(name = "Session.findByAgent", query = "SELECT s FROM Session s WHERE s.agentId = :agentId ORDER BY s.lastAccessedAt DESC")
@NamedQuery(name = "Session.findInactive", query = "SELECT s FROM Session s WHERE s.lastAccessedAt < :threshold")
public class Session extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 会话 ID（业务键） */
    @Column(nullable = false, length = 64, unique = true)
    public String sessionId;

    /** Agent ID */
    @Column(nullable = false, length = 64)
    public String agentId;

    /** 用户 ID（可选） */
    @Column(length = 64)
    public String userId;

    /** 会话标题 */
    public String title;

    /** 上下文变量（JSON） */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "agent_session_context", joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "context_key")
    @Column(name = "context_value", length = 2048)
    public Map<String, String> context = new HashMap<>();

    /** 创建时间 */
    @Column(nullable = false)
    public LocalDateTime createdAt;

    /** 最后访问时间 */
    @Column(nullable = false)
    public LocalDateTime lastAccessedAt;

    /** 消息计数 */
    @Column(nullable = false)
    public int messageCount = 0;

    /** 是否已归档 */
    @Column(nullable = false)
    public boolean archived = false;

    /**
     * 按会话 ID 查找
     */
    public static Session findBySessionId(String sessionId) {
        return getEntityManager()
                .createNamedQuery("Session.findBySessionId", Session.class)
                .setParameter("sessionId", sessionId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 按 Agent 查找会话
     */
    public static java.util.List<Session> findByAgent(String agentId) {
        return getEntityManager()
                .createNamedQuery("Session.findByAgent", Session.class)
                .setParameter("agentId", agentId)
                .getResultList();
    }

    /**
     * 查找不活跃会话
     */
    public static java.util.List<Session> findInactive(LocalDateTime threshold) {
        return getEntityManager()
                .createNamedQuery("Session.findInactive", Session.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }

    /**
     * 更新上下文变量
     */
    public void setContextVariable(String key, String value) {
        this.context.put(key, value);
    }

    /**
     * 获取上下文变量
     */
    public String getContextVariable(String key) {
        return this.context.get(key);
    }

    /**
     * 记录消息
     */
    public void recordMessage() {
        this.messageCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastAccessedAt == null) {
            lastAccessedAt = createdAt;
        }
    }
}
