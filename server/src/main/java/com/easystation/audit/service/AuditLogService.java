package com.easystation.audit.service;

import com.easystation.audit.domain.AuditLog;
import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuditLogService {

    public List<AuditRecord.Detail> list(AuditRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.username() != null && !query.username().isBlank()) {
            sql.append(" and username = :username");
            params.put("username", query.username());
        }
        if (query.userId() != null) {
            sql.append(" and userId = :userId");
            params.put("userId", query.userId());
        }
        if (query.action() != null) {
            sql.append(" and action = :action");
            params.put("action", query.action());
        }
        if (query.result() != null) {
            sql.append(" and result = :result");
            params.put("result", query.result());
        }
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            sql.append(" and resourceType = :resourceType");
            params.put("resourceType", query.resourceType());
        }
        if (query.resourceId() != null) {
            sql.append(" and resourceId = :resourceId");
            params.put("resourceId", query.resourceId());
        }
        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and (description like :keyword or details like :keyword)");
            params.put("keyword", "%" + query.keyword() + "%");
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return AuditLog.<AuditLog>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AuditRecord.Detail get(UUID id) {
        AuditLog log = AuditLog.findById(id);
        if (log == null) {
            return null;
        }
        return toDetail(log);
    }

    @Transactional
    public void record(AuditRecord.Create dto) {
        AuditLog auditLog = new AuditLog();
        auditLog.username = dto.username();
        auditLog.userId = dto.userId();
        auditLog.action = dto.action();
        auditLog.result = dto.result();
        auditLog.description = dto.description();
        auditLog.resourceType = dto.resourceType();
        auditLog.resourceId = dto.resourceId();
        auditLog.details = dto.details();
        auditLog.requestParams = dto.requestParams();
        auditLog.responseResult = dto.responseResult();
        auditLog.clientIp = dto.clientIp();
        auditLog.userAgent = dto.userAgent();
        auditLog.requestPath = dto.requestPath();
        auditLog.requestMethod = dto.requestMethod();
        auditLog.duration = dto.duration();
        auditLog.errorMessage = dto.errorMessage();
        auditLog.persist();

        Log.infof("Audit log recorded: %s by %s - %s", dto.action(), dto.username(), dto.result());
    }

    /**
     * 快捷记录审计日志
     */
    @Transactional
    public void log(String username, UUID userId, AuditAction action, AuditResult result,
                    String description, String resourceType, UUID resourceId,
                    String clientIp, String requestPath) {
        AuditLog auditLog = new AuditLog();
        auditLog.username = username;
        auditLog.userId = userId;
        auditLog.action = action;
        auditLog.result = result;
        auditLog.description = description;
        auditLog.resourceType = resourceType;
        auditLog.resourceId = resourceId;
        auditLog.clientIp = clientIp;
        auditLog.requestPath = requestPath;
        auditLog.persist();
    }

    public AuditRecord.Summary getSummary() {
        long total = AuditLog.count();
        long successCount = AuditLog.count("result", AuditResult.SUCCESS);
        long failedCount = AuditLog.count("result", AuditResult.FAILED);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayCount = AuditLog.count("createdAt >= ?1 and createdAt <= ?2", todayStart, todayEnd);

        return new AuditRecord.Summary(total, successCount, failedCount, todayCount);
    }

    public Map<String, Long> countByAction(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime GROUP BY a.action", Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put(((AuditAction) row[0]).name(), (Long) row[1]);
        }
        return counts;
    }

    public Map<String, Long> countByUser(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery("SELECT a.username, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime GROUP BY a.username ORDER BY COUNT(a) DESC", Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setMaxResults(limit)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return counts;
    }

    private AuditRecord.Detail toDetail(AuditLog log) {
        return new AuditRecord.Detail(
                log.id,
                log.username,
                log.userId,
                log.action,
                log.result,
                log.description,
                log.resourceType,
                log.resourceId,
                log.details,
                log.requestParams,
                log.responseResult,
                log.clientIp,
                log.userAgent,
                log.requestPath,
                log.requestMethod,
                log.duration,
                log.errorMessage,
                log.createdAt
        );
    }
}