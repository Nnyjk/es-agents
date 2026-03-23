package com.easystation.profile.service;

import com.easystation.profile.dto.PreferenceRecord;
import com.easystation.profile.dto.ProfileRecord;
import com.easystation.profile.domain.UserPreference;
import com.easystation.profile.mapper.PreferenceMapper;
import com.easystation.profile.mapper.ProfileMapper;
import com.easystation.profile.repository.PreferenceRepository;
import com.easystation.system.domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@ApplicationScoped
public class ProfileService {

    @Inject
    PreferenceRepository preferenceRepository;

    @Inject
    ProfileMapper profileMapper;

    @Inject
    PreferenceMapper preferenceMapper;

    public ProfileRecord getProfile(UUID userId) {
        User user = User.<User>findByIdOptional(userId)
            .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
        return profileMapper.toRecord(user);
    }

    @Transactional
    public ProfileRecord updateProfile(UUID userId, ProfileRecord.Update dto) {
        User user = User.<User>findByIdOptional(userId)
            .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
        
        profileMapper.updateEntity(dto, user);
        user.persist();
        
        return profileMapper.toRecord(user);
    }

    @Transactional
    public void changePassword(UUID userId, ProfileRecord.PasswordChange dto) {
        // TODO: Implement password change logic
        // 1. Verify old password using UserService or AuthRepository
        // 2. Hash new password
        // 3. Update password in user entity
        // 4. Optionally terminate all other sessions
        
        // Placeholder for now - integration with auth module needed
        throw new WebApplicationException("Password change not yet implemented", Response.Status.NOT_IMPLEMENTED);
    }

    public PreferenceRecord getPreference(UUID userId) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        return preferenceMapper.toRecord(preference);
    }

    @Transactional
    public PreferenceRecord updatePreference(UUID userId, PreferenceRecord.Update dto) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        
        preferenceMapper.updateEntity(dto, preference);
        preferenceRepository.persist(preference);
        
        return preferenceMapper.toRecord(preference);
    }

    private UserPreference createDefaultPreference(UUID userId) {
        UserPreference preference = new UserPreference();
        preference.userId = userId;
        preference.theme = "light";
        preference.language = "zh-CN";
        preference.layout = "default";
        preference.pageSize = 20;
        preferenceRepository.persist(preference);
        return preference;
    }
}