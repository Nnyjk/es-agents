package com.easystation.agent.repository;

import com.easystation.agent.domain.SystemEventLog;
import com.easystation.agent.dto.SystemEventLogDTO.EventQueryCriteria;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统事件日志 Repository
 */
@ApplicationScoped
public class SystemEventLogRepository implements PanacheRepository<SystemEventLog> {

    /**
     * 根据条件查询事件日志
     */
    public List<SystemEventLog> findAllByFilters(EventQueryCriteria criteria, int page, int size) {
        StringBuilder jpql = new StringBuilder("FROM SystemEventLog WHERE 1=1");
        
        if (criteria.eventType() != null && !criteria.eventType().isBlank()) {
            jpql.append(" AND eventType = :eventType");
        }
        if (criteria.eventLevel() != null && !criteria.eventLevel().isBlank()) {
            jpql.append(" AND eventLevel = :eventLevel");
        }
        if (criteria.module() != null && !criteria.module().isBlank()) {
            jpql.append(" AND module = :module");
        }
        if (criteria.operation() != null && !criteria.operation().isBlank()) {
            jpql.append(" AND operation = :operation");
        }
        if (criteria.targetType() != null && !criteria.targetType().isBlank()) {
            jpql.append(" AND targetType = :targetType");
        }
        if (criteria.targetId() != null) {
            jpql.append(" AND targetId = :targetId");
        }
        if (criteria.userId() != null) {
            jpql.append(" AND userId = :userId");
        }
        if (criteria.startTime() != null) {
            jpql.append(" AND createdAt >= :startTime");
        }
        if (criteria.endTime() != null) {
            jpql.append(" AND createdAt <= :endTime");
        }
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            jpql.append(" AND (LOWER(message) LIKE :keyword OR LOWER(details) LIKE :keyword)");
        }
        
        jpql.append(" ORDER BY createdAt DESC");
        
        Query q = getEntityManager().createQuery(jpql.toString())
            .setMaxResults(size)
            .setFirstResult(page * size);
        
        setParameters(q, criteria);
        
        return q.getResultList();
    }

    /**
     * 统计符合条件的记录数
     */
    public long countByFilters(EventQueryCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM SystemEventLog e WHERE 1=1");
        
        if (criteria.eventType() != null && !criteria.eventType().isBlank()) {
            jpql.append(" AND e.eventType = :eventType");
        }
        if (criteria.eventLevel() != null && !criteria.eventLevel().isBlank()) {
            jpql.append(" AND e.eventLevel = :eventLevel");
        }
        if (criteria.module() != null && !criteria.module().isBlank()) {
            jpql.append(" AND e.module = :module");
        }
        if (criteria.operation() != null && !criteria.operation().isBlank()) {
            jpql.append(" AND e.operation = :operation");
        }
        if (criteria.targetType() != null && !criteria.targetType().isBlank()) {
            jpql.append(" AND e.targetType = :targetType");
        }
        if (criteria.targetId() != null) {
            jpql.append(" AND e.targetId = :targetId");
        }
        if (criteria.userId() != null) {
            jpql.append(" AND e.userId = :userId");
        }
        if (criteria.startTime() != null) {
            jpql.append(" AND e.createdAt >= :startTime");
        }
        if (criteria.endTime() != null) {
            jpql.append(" AND e.createdAt <= :endTime");
        }
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            jpql.append(" AND (LOWER(e.message) LIKE :keyword OR LOWER(e.details) LIKE :keyword)");
        }
        
        Query q = getEntityManager().createQuery(jpql.toString());
        setParameters(q, criteria);
        
        return (Long) q.getSingleResult();
    }

    /**
     * 删除指定时间之前的日志
     */
    public long deleteByCreatedAtBefore(LocalDateTime date) {
        return delete("createdAt < ?1", date);
    }

    /**
     * 设置查询参数
     */
    private void setParameters(Query q, EventQueryCriteria criteria) {
        if (criteria.eventType() != null && !criteria.eventType().isBlank()) {
            q.setParameter("eventType", criteria.eventType());
        }
        if (criteria.eventLevel() != null && !criteria.eventLevel().isBlank()) {
            q.setParameter("eventLevel", criteria.eventLevel());
        }
        if (criteria.module() != null && !criteria.module().isBlank()) {
            q.setParameter("module", criteria.module());
        }
        if (criteria.operation() != null && !criteria.operation().isBlank()) {
            q.setParameter("operation", criteria.operation());
        }
        if (criteria.targetType() != null && !criteria.targetType().isBlank()) {
            q.setParameter("targetType", criteria.targetType());
        }
        if (criteria.targetId() != null) {
            q.setParameter("targetId", criteria.targetId());
        }
        if (criteria.userId() != null) {
            q.setParameter("userId", criteria.userId());
        }
        if (criteria.startTime() != null) {
            q.setParameter("startTime", criteria.startTime());
        }
        if (criteria.endTime() != null) {
            q.setParameter("endTime", criteria.endTime());
        }
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            q.setParameter("keyword", "%" + criteria.keyword().toLowerCase() + "%");
        }
    }
}
