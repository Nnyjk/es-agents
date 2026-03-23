package com.easystation.profile.mapper;

import com.easystation.profile.dto.PreferenceRecord;
import com.easystation.profile.domain.UserPreference;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class PreferenceMapper {

    public PreferenceRecord toRecord(UserPreference entity) {
        if (entity == null) {
            return null;
        }
        
        List<String> displayFields = parseStringList(entity.displayFields);
        List<PreferenceRecord.QuickAction> quickActions = parseQuickActions(entity.quickActions);
        
        return new PreferenceRecord(
            entity.id,
            entity.theme,
            entity.language,
            entity.layout,
            entity.defaultPage,
            entity.pageSize,
            entity.defaultSort,
            displayFields,
            quickActions,
            entity.notificationEnabled,
            entity.emailNotification,
            entity.smsNotification,
            entity.webhookNotification,
            entity.silentHoursStart,
            entity.silentHoursEnd,
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
        if (dto.defaultPage() != null) {
            entity.defaultPage = dto.defaultPage();
        }
        if (dto.pageSize() != null) {
            entity.pageSize = dto.pageSize();
        }
        if (dto.defaultSort() != null) {
            entity.defaultSort = dto.defaultSort();
        }
        if (dto.displayFields() != null) {
            entity.displayFields = String.join(",", dto.displayFields());
        }
        if (dto.quickActions() != null) {
            entity.quickActions = serializeQuickActions(dto.quickActions());
        }
        if (dto.notificationEnabled() != null) {
            entity.notificationEnabled = dto.notificationEnabled();
        }
        if (dto.emailNotification() != null) {
            entity.emailNotification = dto.emailNotification();
        }
        if (dto.smsNotification() != null) {
            entity.smsNotification = dto.smsNotification();
        }
        if (dto.webhookNotification() != null) {
            entity.webhookNotification = dto.webhookNotification();
        }
        if (dto.silentHoursStart() != null) {
            entity.silentHoursStart = dto.silentHoursStart();
        }
        if (dto.silentHoursEnd() != null) {
            entity.silentHoursEnd = dto.silentHoursEnd();
        }
    }

    private List<String> parseStringList(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split(","));
    }

    private List<PreferenceRecord.QuickAction> parseQuickActions(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        // Simple JSON array parsing - could use Jackson for better parsing
        // Format: [{"key":"k","label":"l","icon":"i","path":"p"},...]
        // For now, return empty list - implement proper JSON parsing if needed
        return Collections.emptyList();
    }

    private String serializeQuickActions(List<PreferenceRecord.QuickActionUpdate> actions) {
        if (actions == null || actions.isEmpty()) {
            return null;
        }
        // Simple serialization - implement proper JSON serialization if needed
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actions.size(); i++) {
            PreferenceRecord.QuickActionUpdate a = actions.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"key\":\"").append(a.key()).append("\"");
            sb.append(",\"label\":\"").append(a.label()).append("\"");
            sb.append(",\"icon\":\"").append(a.icon() != null ? a.icon() : "").append("\"");
            sb.append(",\"path\":\"").append(a.path()).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }
}