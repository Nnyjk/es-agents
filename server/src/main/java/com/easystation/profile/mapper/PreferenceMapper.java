package com.easystation.profile.mapper;

import com.easystation.profile.dto.PreferenceRecord;
import com.easystation.profile.domain.UserPreference;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PreferenceMapper {

    public PreferenceRecord toRecord(UserPreference entity) {
        if (entity == null) {
            return null;
        }
        return new PreferenceRecord(
            entity.id,
            entity.userId,
            entity.theme,
            entity.language,
            entity.layout,
            entity.pageSize,
            entity.timezone,
            entity.dateFormat,
            entity.timeFormat,
            entity.emailNotification,
            entity.pushNotification,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public void updateEntity(PreferenceRecord.Update dto, UserPreference entity) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.theme() != null) {
            entity.theme = dto.theme();
        }
        if (dto.language() != null) {
            entity.language = dto.language();
        }
        if (dto.layout() != null) {
            entity.layout = dto.layout();
        }
        if (dto.pageSize() != null) {
            entity.pageSize = dto.pageSize();
        }
        if (dto.timezone() != null) {
            entity.timezone = dto.timezone();
        }
        if (dto.dateFormat() != null) {
            entity.dateFormat = dto.dateFormat();
        }
        if (dto.timeFormat() != null) {
            entity.timeFormat = dto.timeFormat();
        }
        if (dto.emailNotification() != null) {
            entity.emailNotification = dto.emailNotification();
        }
        if (dto.pushNotification() != null) {
            entity.pushNotification = dto.pushNotification();
        }
    }
}