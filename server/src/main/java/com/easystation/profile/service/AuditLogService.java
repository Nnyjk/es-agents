package com.easystation.profile.service;

import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.mapper.AuditLogMapper;
import com.easystation.profile.repository.AuditLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuditLogService {

    @Inject
    AuditLogRepository auditLogRepository;

    @Inject
    AuditLogMapper auditLogMapper;

    public List<AuditLogRecord> listLogs(UUID userId, AuditLogRecord.Query query) {
        List<UserAuditLog> logs = auditLogRepository.findByUserId(userId, query);
        return logs.stream()
            .map(auditLogMapper::toRecord)
            .collect(Collectors.toList());
    }

    public AuditLogRecord.Summary getSummary(UUID userId) {
        long totalCount = auditLogRepository.countByUserId(userId);
        long successCount = auditLogRepository.countByUserIdAndStatus(userId, "SUCCESS");
        long failedCount = auditLogRepository.countByUserIdAndStatus(userId, "FAILED");
        
        // Get top 5 actions
        List<AuditLogRecord.ActionCount> topActions = auditLogRepository.findTopActionsByUserId(userId, 5)
            .stream()
            .map(row -> new AuditLogRecord.ActionCount((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
        
        return new AuditLogRecord.Summary(totalCount, successCount, failedCount, topActions);
    }

    @Transactional
    public void logSuccess(UUID userId, String action, String resourceType, String resourceId,
                          String description, String oldValues, String newValues, Long duration) {
        UserAuditLog log = new UserAuditLog();
        log.userId = userId;
        log.action = action;
        log.resourceType = resourceType;
        log.resourceId = resourceId;
        log.description = description;
        log.status = "success";
        log.requestData = oldValues;
        log.responseData = newValues;
        log.durationMs = duration;
        
        auditLogRepository.persist(log);
    }

    @Transactional
    public void logFailure(UUID userId, String action, String resourceType, String resourceId,
                          String description, String errorMessage) {
        UserAuditLog log = new UserAuditLog();
        log.userId = userId;
        log.action = action;
        log.resourceType = resourceType;
        log.resourceId = resourceId;
        log.description = description;
        log.status = "failure";
        log.errorMessage = errorMessage;
        
        auditLogRepository.persist(log);
    }
}