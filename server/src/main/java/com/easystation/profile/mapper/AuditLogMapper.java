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
}