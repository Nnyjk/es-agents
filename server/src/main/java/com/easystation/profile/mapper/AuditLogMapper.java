package com.easystation.profile.mapper;

import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.domain.UserAuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuditLogMapper {

    @Mapping(target = "oldValues", source = "requestData")
    @Mapping(target = "newValues", source = "responseData")
    @Mapping(target = "duration", source = "durationMs")
    AuditLogRecord toRecord(UserAuditLog entity);
}