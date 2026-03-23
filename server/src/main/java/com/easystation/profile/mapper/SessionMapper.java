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
        
        // Parse device info from deviceInfo string (simple implementation)
        String deviceName = entity.deviceInfo;
        String deviceType = "unknown";
        String os = "unknown";
        String browser = "unknown";
        
        if (entity.deviceInfo != null) {
            deviceName = entity.deviceInfo;
            // Simple parsing - could be enhanced with proper UA parsing
            String ua = entity.deviceInfo.toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                deviceType = "mobile";
            } else if (ua.contains("tablet") || ua.contains("ipad")) {
                deviceType = "tablet";
            } else {
                deviceType = "desktop";
            }
            
            if (ua.contains("windows")) {
                os = "Windows";
            } else if (ua.contains("mac")) {
                os = "macOS";
            } else if (ua.contains("linux")) {
                os = "Linux";
            } else if (ua.contains("android")) {
                os = "Android";
            } else if (ua.contains("ios") || ua.contains("iphone") || ua.contains("ipad")) {
                os = "iOS";
            }
            
            if (ua.contains("chrome") && !ua.contains("edg")) {
                browser = "Chrome";
            } else if (ua.contains("firefox")) {
                browser = "Firefox";
            } else if (ua.contains("safari") && !ua.contains("chrome")) {
                browser = "Safari";
            } else if (ua.contains("edg")) {
                browser = "Edge";
            }
        }
        
        return new SessionRecord(
            entity.id,
            entity.tokenId,
            entity.deviceInfo,
            deviceName,
            deviceType,
            os,
            browser,
            entity.ipAddress,
            "unknown", // location - would need GeoIP service
            entity.lastActivityAt,
            entity.createdAt,
            entity.expiresAt,
            entity.isActive,
            isCurrent
        );
    }
}