package com.easystation.profile.service;

import com.easystation.profile.dto.NotificationSubscriptionRecord;
import com.easystation.profile.domain.UserNotificationSubscription;
import com.easystation.profile.mapper.NotificationSubscriptionMapper;
import com.easystation.profile.repository.NotificationSubscriptionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationSubscriptionService {

    @Inject
    NotificationSubscriptionRepository subscriptionRepository;

    @Inject
    NotificationSubscriptionMapper subscriptionMapper;

    public List<NotificationSubscriptionRecord> listSubscriptions(UUID userId) {
        List<UserNotificationSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        
        // If no subscriptions, create defaults
        if (subscriptions.isEmpty()) {
            return createDefaultSubscriptions(userId);
        }
        
        return subscriptions.stream()
            .map(subscriptionMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Transactional
    public NotificationSubscriptionRecord updateSubscription(UUID userId, NotificationSubscriptionRecord.Update dto) {
        UserNotificationSubscription subscription = subscriptionRepository
            .findByUserIdAndType(userId, dto.notificationType())
            .orElseGet(() -> createSubscription(userId, dto.notificationType()));
        
        subscription.enabled = dto.enabled();
        
        subscriptionRepository.persist(subscription);
        
        return subscriptionMapper.toRecord(subscription);
    }

    @Transactional
    public List<NotificationSubscriptionRecord> batchUpdate(UUID userId, NotificationSubscriptionRecord.BatchUpdate dto) {
        List<NotificationSubscriptionRecord> results = new ArrayList<>();
        
        for (NotificationSubscriptionRecord.Update update : dto.updates()) {
            results.add(updateSubscription(userId, update));
        }
        
        return results;
    }

    private List<NotificationSubscriptionRecord> createDefaultSubscriptions(UUID userId) {
        String[] defaultTypes = {
            "security", "system", "product", "marketing", "social"
        };
        
        List<NotificationSubscriptionRecord> subscriptions = new ArrayList<>();
        
        for (String type : defaultTypes) {
            UserNotificationSubscription sub = createSubscription(userId, type);
            
            // Set defaults based on type
            sub.enabled = true;
            
            subscriptionRepository.persist(sub);
            subscriptions.add(subscriptionMapper.toRecord(sub));
        }
        
        return subscriptions;
    }

    private UserNotificationSubscription createSubscription(UUID userId, String notificationType) {
        UserNotificationSubscription subscription = new UserNotificationSubscription();
        subscription.userId = userId;
        subscription.notificationType = notificationType;
        subscription.enabled = true;
        return subscription;
    }
}