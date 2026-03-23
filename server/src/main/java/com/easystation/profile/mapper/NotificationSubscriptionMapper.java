package com.easystation.profile.mapper;

import com.easystation.profile.dto.NotificationSubscriptionRecord;
import com.easystation.profile.domain.UserNotificationSubscription;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class NotificationSubscriptionMapper {

    public NotificationSubscriptionRecord toRecord(UserNotificationSubscription entity) {
        if (entity == null) {
            return null;
        }
        
        List<String> channels = parseChannels(entity.channels);
        
        return new NotificationSubscriptionRecord(
            entity.id,
            entity.notificationType,
            entity.enabled,
            channels,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public void updateEntity(NotificationSubscriptionRecord.Update dto, UserNotificationSubscription entity) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.enabled() != null) {
            entity.enabled = dto.enabled();
        }
        if (dto.channels() != null) {
            entity.channels = String.join(",", dto.channels());
        }
    }

    private List<String> parseChannels(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split(","));
    }
}