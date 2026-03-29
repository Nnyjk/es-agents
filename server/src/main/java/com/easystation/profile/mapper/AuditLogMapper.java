package com.easystation.profile.mapper;

import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.domain.UserAuditLog;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditLogMapper {

    public AuditLogRecord toRecord(UserAuditLog entity) {
        if (entity == null) {
            return null;
        }
        return new AuditLogRecord(
            entity.id,
            entity.action,
            entity.resourceType,
            entity.resourceId,
            entity.description,
            entity.ipAddress,
            entity.userAgent,
            entity.status,
            entity.errorMessage,
            entity.durationMs,
            entity.createdAt
        );
    }

    public UserAuditLog toEntity(AuditLogRecord record) {
        if (record == null) {
            return null;
        }
        UserAuditLog log = new UserAuditLog();
        log.id = record.id();
        log.action = record.action();
        log.resourceType = record.resourceType();
        log.resourceId = record.resourceId();
        log.description = record.description();
        log.ipAddress = record.ipAddress();
        log.userAgent = record.userAgent();
        log.status = record.status();
        log.errorMessage = record.errorMessage();
        log.durationMs = record.durationMs();
        log.createdAt = record.createdAt();
        return log;
    }
}