package com.easystation.profile.mapper;

import com.easystation.profile.dto.ProfileRecord;
import com.easystation.system.domain.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfileMapper {

    public ProfileRecord toRecord(User entity) {
        if (entity == null) {
            return null;
        }
        return new ProfileRecord(
            entity.id,
            entity.username,
            entity.email,
            entity.phone,
            entity.nickname,
            entity.avatar,
            entity.mfaEnabled,
            entity.status != null ? entity.status.name() : null,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public void updateEntity(ProfileRecord.Update dto, User entity) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.email() != null) {
            entity.email = dto.email();
        }
        if (dto.phone() != null) {
            entity.phone = dto.phone();
        }
        if (dto.nickname() != null) {
            entity.nickname = dto.nickname();
        }
        if (dto.avatar() != null) {
            entity.avatar = dto.avatar();
        }
    }
}