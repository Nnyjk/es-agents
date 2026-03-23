package com.easystation.profile.mapper;

import com.easystation.profile.dto.NotificationSubscriptionRecord;
import com.easystation.profile.domain.UserNotificationSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationSubscriptionMapper {

    @Mapping(target = "emailEnabled", constant = "false")
    @Mapping(target = "pushEnabled", constant = "false")
    @Mapping(target = "smsEnabled", constant = "false")
    @Mapping(target = "digestEnabled", constant = "false")
    NotificationSubscriptionRecord toRecord(UserNotificationSubscription entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "channels", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(NotificationSubscriptionRecord.Update dto, @MappingTarget UserNotificationSubscription entity);
}