package com.easystation.profile.mapper;

import com.easystation.profile.dto.SessionRecord;
import com.easystation.profile.domain.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SessionMapper {

    @Mapping(target = "isCurrent", expression = "java(false)")
    @Mapping(target = "deviceName", source = "deviceInfo")
    @Mapping(target = "deviceType", constant = "unknown")
    @Mapping(target = "os", constant = "unknown")
    @Mapping(target = "browser", constant = "unknown")
    @Mapping(target = "location", constant = "unknown")
    @Mapping(target = "lastActiveAt", source = "lastActivityAt")
    SessionRecord toRecord(UserSession entity);

    default SessionRecord toRecord(UserSession entity, String currentTokenId) {
        SessionRecord record = toRecord(entity);
        return new SessionRecord(
            record.id(),
            record.userId(),
            record.deviceName(),
            record.deviceType(),
            record.os(),
            record.browser(),
            record.ipAddress(),
            record.location(),
            record.loginAt(),
            record.lastActiveAt(),
            record.expiresAt(),
            currentTokenId != null && currentTokenId.equals(entity.tokenId)
        );
    }
}