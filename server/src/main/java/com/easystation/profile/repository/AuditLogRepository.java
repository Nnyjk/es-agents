package com.easystation.profile.repository;

import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.domain.UserAuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuditLogRepository implements PanacheRepository<UserAuditLog> {

    public List<UserAuditLog> findByUserId(UUID userId, AuditLogRecord.Query query) {
        StringBuilder jpql = new StringBuilder("FROM UserAuditLog WHERE userId = :userId");
        
        if (query.keyword() != null && !query.keyword().isBlank()) {
            jpql.append(" AND (LOWER(action) LIKE :keyword OR LOWER(description) LIKE :keyword)");
        }
        if (query.action() != null && !query.action().isBlank()) {
            jpql.append(" AND action = :action");
        }
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            jpql.append(" AND resourceType = :resourceType");
        }
        if (query.status() != null && !query.status().isBlank()) {
            jpql.append(" AND status = :status");
        }
        if (query.startTime() != null) {
            jpql.append(" AND createdAt >= :startTime");
        }
        if (query.endTime() != null) {
            jpql.append(" AND createdAt <= :endTime");
        }
        
        jpql.append(" ORDER BY createdAt DESC");
        
        Query q = getEntityManager().createQuery(jpql.toString())
            .setParameter("userId", userId)
            .setMaxResults(query.limit() != null ? query.limit() : 50)
            .setFirstResult(query.offset() != null ? query.offset() : 0);
        
        if (query.keyword() != null && !query.keyword().isBlank()) {
            q.setParameter("keyword", "%" + query.keyword().toLowerCase() + "%");
        }
        if (query.action() != null && !query.action().isBlank()) {
            q.setParameter("action", query.action());
        }
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            q.setParameter("resourceType", query.resourceType());
        }
        if (query.status() != null && !query.status().isBlank()) {
            q.setParameter("status", query.status());
        }
        if (query.startTime() != null) {
            q.setParameter("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            q.setParameter("endTime", query.endTime());
        }
        
        return q.getResultList();
    }

    public long countByUserIdAndTimeRange(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        return count("userId = ?1 AND createdAt >= ?2 AND createdAt <= ?3", userId, startTime, endTime);
    }

    public long countByUserIdAndAction(UUID userId, String action, LocalDateTime startTime, LocalDateTime endTime) {
        return count("userId = ?1 AND action = ?2 AND createdAt >= ?3 AND createdAt <= ?4", 
            userId, action, startTime, endTime);
    }

    public long countByUserId(UUID userId) {
        return count("userId", userId);
    }

    public long countByUserIdAndStatus(UUID userId, String status) {
        return count("userId = ?1 AND status = ?2", userId, status);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findTopActionsByUserId(UUID userId, int limit) {
        String jpql = "SELECT action, COUNT(*) as cnt FROM UserAuditLog WHERE userId = :userId GROUP BY action ORDER BY cnt DESC";
        return getEntityManager().createQuery(jpql)
            .setParameter("userId", userId)
            .setMaxResults(limit)
            .getResultList();
    }

    // ========== 新增方法：审计日志增强查询 ==========

    /**
     * 按小时统计失败操作次数
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countFailuresByHour(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        String jpql = "SELECT DATE_TRUNC('hour', createdAt) as hour, COUNT(*) as cnt " +
            "FROM UserAuditLog WHERE userId = :userId AND status = 'failure' " +
            "AND createdAt >= :startTime AND createdAt <= :endTime " +
            "GROUP BY DATE_TRUNC('hour', createdAt) ORDER BY hour";
        return getEntityManager().createQuery(jpql)
            .setParameter("userId", userId)
            .setParameter("startTime", startTime)
            .setParameter("endTime", endTime)
            .getResultList();
    }

    /**
     * 查找用户的敏感操作日志
     */
    public List<UserAuditLog> findSensitiveByUser(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        return find("userId = ?1 AND isSensitive = true AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
            userId, startTime, endTime).list();
    }

    /**
     * 查找用户的常用 IP 地址
     */
    @SuppressWarnings("unchecked")
    public List<String> findCommonIPs(UUID userId, int limit) {
        String jpql = "SELECT ipAddress FROM UserAuditLog WHERE userId = :userId AND ipAddress IS NOT NULL " +
            "GROUP BY ipAddress ORDER BY COUNT(*) DESC";
        return getEntityManager().createQuery(jpql)
            .setParameter("userId", userId)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * 查找带 IP 的日志
     */
    public List<UserAuditLog> findByUserIdWithIP(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        return find("userId = ?1 AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
            userId, startTime, endTime).list();
    }

    /**
     * 查找批量操作日志
     */
    public List<UserAuditLog> findBatchOperations(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        return find("userId = ?1 AND (action LIKE ?2 OR action LIKE ?3) AND createdAt >= ?4 AND createdAt <= ?5 ORDER BY createdAt DESC",
            userId, "BATCH_%", "BATCH_%", startTime, endTime).list();
    }

    /**
     * 查找需要审查的日志
     */
    public List<UserAuditLog> findRequiresReview() {
        return find("requiresReview = true AND reviewStatus = 'PENDING' ORDER BY createdAt DESC").list();
    }

    /**
     * 按风险等级查找日志
     */
    public List<UserAuditLog> findByRiskLevel(String riskLevel, LocalDateTime startTime, LocalDateTime endTime) {
        return find("riskLevel = ?1 AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
            riskLevel, startTime, endTime).list();
    }

    /**
     * 导出查询 - 支持大结果集
     */
    public List<UserAuditLog> findForExport(UUID userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return find("userId = ?1 AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
            userId, startTime, endTime)
            .page(0, limit)
            .list();
    }

    /**
     * 验证日志完整性时批量查询
     */
    public List<UserAuditLog> findByIds(List<UUID> ids) {
        return find("id IN ?1", ids).list();
    }
}