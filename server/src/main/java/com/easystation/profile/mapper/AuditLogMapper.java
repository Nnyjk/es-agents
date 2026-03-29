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
        UserAuditLog entity = new UserAuditLog();
        entity.id = record.id();
        entity.action = record.action();
        entity.resourceType = record.resourceType();
        entity.resourceId = record.resourceId();
        entity.description = record.description();
        entity.ipAddress = record.ipAddress();
        entity.userAgent = record.userAgent();
        entity.status = record.status();
        entity.errorMessage = record.errorMessage();
        entity.durationMs = record.durationMs();
        entity.createdAt = record.createdAt();
        return entity;
    }
}