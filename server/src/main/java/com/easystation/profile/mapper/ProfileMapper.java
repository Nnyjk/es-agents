package com.easystation.profile.mapper;

import com.easystation.profile.dto.ProfileRecord;
import com.easystation.system.domain.User;
import com.easystation.system.domain.Role;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfileMapper {

    public ProfileRecord toRecord(User entity) {
        if (entity == null) {
            return null;
        }
        
        Set<ProfileRecord.RoleInfo> roles = null;
        if (entity.roles != null) {
            roles = entity.roles.stream()
                .map(role -> new ProfileRecord.RoleInfo(
                    role.id,
                    role.code,
                    role.name
                ))
                .collect(Collectors.toSet());
        }
        
        return new ProfileRecord(
            entity.id,
            entity.username,
            entity.email,
            entity.phone,
            entity.nickname,
            entity.avatar,
            entity.mfaEnabled,
            entity.status,
            roles,
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