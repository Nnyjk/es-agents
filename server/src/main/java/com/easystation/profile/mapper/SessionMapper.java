package com.easystation.profile.mapper;

import com.easystation.profile.dto.SessionRecord;
import com.easystation.profile.domain.UserSession;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionMapper {

    public SessionRecord toRecord(UserSession entity) {
        return toRecord(entity, null);
    }

    public SessionRecord toRecord(UserSession entity, String currentTokenId) {
        if (entity == null) {
            return null;
        }
        
        boolean isCurrent = currentTokenId != null && currentTokenId.equals(entity.tokenId);
        
        return new SessionRecord(
            entity.id,
            entity.deviceInfo,
            entity.ipAddress,
            entity.userAgent,
            entity.loginAt,
            entity.lastActivityAt,
            isCurrent
        );
    }
}