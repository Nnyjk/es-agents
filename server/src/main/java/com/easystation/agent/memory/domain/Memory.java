package com.easystation.agent.memory.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 记忆实体
 * 支持短期和长期记忆存储
 */
@Entity
@Table(name = "agent_memory")
@NamedQuery(name = "Memory.findBySession", query = "SELECT m FROM Memory m WHERE m.sessionId = :sessionId ORDER BY m.createdAt DESC")
@NamedQuery(name = "Memory.findShortTermBySession", query = "SELECT m FROM Memory m WHERE m.sessionId = :sessionId AND m.type = :type ORDER BY m.createdAt DESC")
@NamedQuery(name = "Memory.findExpired", query = "SELECT m FROM Memory m WHERE m.expiresAt IS NOT NULL AND m.expiresAt < :now")
public class Memory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 会话 ID */
    @Column(nullable = false, length = 64)
    public String sessionId;

    /** 记忆类型 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MemoryType type;

    /** 记忆内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    /** 元数据（JSON） */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "agent_memory_metadata", joinColumns = @JoinColumn(name = "memory_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", length = 1024)
    public Map<String, String> metadata = new HashMap<>();

    /** 创建时间 */
    @Column(nullable = false)
    public LocalDateTime createdAt;

    /** 过期时间（仅短期记忆） */
    public LocalDateTime expiresAt;

    /** 最后访问时间 */
    public LocalDateTime lastAccessedAt;

    /** 访问次数 */
    @Column(nullable = false)
    public int accessCount = 0;

    /** 重要性评分 */
    @Enumerated(EnumType.STRING)
    public MemoryImportance importance = MemoryImportance.MEDIUM;

    /** 向量嵌入（用于相似度搜索） */
    @Column(columnDefinition = "vector(768)")
    public float[] embedding;

    /** 是否已压缩 */
    @Column(nullable = false)
    public boolean compressed = false;

    /** 父记忆 ID（压缩后） */
    public UUID parentMemoryId;

    /**
     * 按会话查找记忆
     */
    public static java.util.List<Memory> findBySession(String sessionId) {
        return find("sessionId", sessionId).list();
    }

    /**
     * 按会话和类型查找记忆
     */
    public static java.util.List<Memory> findBySessionAndType(String sessionId, MemoryType type) {
        return getEntityManager()
                .createNamedQuery("Memory.findShortTermBySession", Memory.class)
                .setParameter("sessionId", sessionId)
                .setParameter("type", type)
                .setMaxResults(100)
                .getResultList();
    }

    /**
     * 查找过期记忆
     */
    public static java.util.List<Memory> findExpired(LocalDateTime now) {
        return getEntityManager()
                .createNamedQuery("Memory.findExpired", Memory.class)
                .setParameter("now", now)
                .getResultList();
    }

    /**
     * 记录访问
     */
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
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
