package com.easystation.profile.mapper;

import com.easystation.profile.dto.NotificationSubscriptionRecord;
import com.easystation.profile.domain.UserNotificationSubscription;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationSubscriptionMapper {

    public NotificationSubscriptionRecord toRecord(UserNotificationSubscription entity) {
        if (entity == null) {
            return null;
        }
        return new NotificationSubscriptionRecord(
            entity.id,
            entity.userId,
            entity.notificationType,
            entity.enabled,
            false, // emailEnabled - not implemented yet
            false, // pushEnabled - not implemented yet
            false, // smsEnabled - not implemented yet
            false, // digestEnabled - not implemented yet
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
    }
}