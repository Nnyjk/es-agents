package com.easystation.profile.repository;

import com.easystation.profile.domain.UserNotificationSubscription;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class NotificationSubscriptionRepository implements PanacheRepository<UserNotificationSubscription> {

    public List<UserNotificationSubscription> findByUserId(UUID userId) {
        return find("userId", userId).list();
    }

    public Optional<UserNotificationSubscription> findByUserIdAndType(UUID userId, String notificationType) {
        return find("userId = ?1 AND notificationType = ?2", userId, notificationType)
            .firstResultOptional();
    }
}