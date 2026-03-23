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
}