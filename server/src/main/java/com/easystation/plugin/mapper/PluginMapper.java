package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.dto.PluginRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginMapper {

    @Inject
    ObjectMapper objectMapper;

    public PluginRecord toRecord(Plugin entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginRecord(
            entity.id,
            entity.developerId,
            null, // developerName - needs to be set by service
            entity.categoryId,
            null, // categoryName - needs to be set by service
            entity.name,
            entity.code,
            entity.icon,
            entity.description,
            entity.readme,
            entity.sourceUrl,
            entity.docUrl,
            entity.homepageUrl,
            entity.status,
            entity.isFree,
            entity.price,
            entity.minPlatformVersion,
            entity.maxPlatformVersion,
            parseJsonToList(entity.supportedPlatforms),
            parseJsonToList(entity.permissionsRequired),
            entity.configSchema,
            entity.totalDownloads,
            entity.totalInstalls,
            entity.averageRating,
            entity.ratingCount,
            entity.commentCount,
            entity.favoriteCount,
            entity.publishedAt,
            entity.createdAt,
            entity.updatedAt,
            null, // tags - needs to be set by service
            null  // latestVersion - needs to be set by service
        );
    }

    public List<PluginRecord> toRecords(List<Plugin> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public Plugin toEntity(PluginRecord.Create create, java.util.UUID developerId) {
        if (create == null) {
            return null;
        }
        
        Plugin entity = new Plugin();
        entity.developerId = developerId;
        entity.categoryId = create.categoryId();
        entity.name = create.name();
        entity.code = create.code();
        entity.icon = create.icon();
        entity.description = create.description();
        entity.readme = create.readme();
        entity.sourceUrl = create.sourceUrl();
        entity.docUrl = create.docUrl();
        entity.homepageUrl = create.homepageUrl();
        entity.isFree = create.isFree() != null ? create.isFree() : true;
        entity.price = create.price();
        entity.minPlatformVersion = create.minPlatformVersion();
        entity.maxPlatformVersion = create.maxPlatformVersion();
        entity.supportedPlatforms = listToJson(create.supportedPlatforms());
        entity.permissionsRequired = listToJson(create.permissionsRequired());
        entity.configSchema = create.configSchema();
        entity.status = com.easystation.plugin.domain.enums.PluginStatus.DRAFT;
        entity.totalDownloads = 0L;
        entity.totalInstalls = 0L;
        entity.averageRating = java.math.BigDecimal.ZERO;
        entity.ratingCount = 0;
        entity.commentCount = 0;
        entity.favoriteCount = 0;
        
        return entity;
    }

    public void updateEntity(Plugin entity, PluginRecord.Update update) {
        if (entity == null || update == null) {
            return;
        }
        
        if (update.categoryId() != null) {
            entity.categoryId = update.categoryId();
        }
        if (update.name() != null) {
            entity.name = update.name();
        }
        if (update.icon() != null) {
            entity.icon = update.icon();
        }
        if (update.description() != null) {
            entity.description = update.description();
        }
        if (update.readme() != null) {
            entity.readme = update.readme();
        }
        if (update.sourceUrl() != null) {
            entity.sourceUrl = update.sourceUrl();
        }
        if (update.docUrl() != null) {
            entity.docUrl = update.docUrl();
        }
        if (update.homepageUrl() != null) {
            entity.homepageUrl = update.homepageUrl();
        }
        if (update.isFree() != null) {
            entity.isFree = update.isFree();
        }
        if (update.price() != null) {
            entity.price = update.price();
        }
        if (update.minPlatformVersion() != null) {
            entity.minPlatformVersion = update.minPlatformVersion();
        }
        if (update.maxPlatformVersion() != null) {
            entity.maxPlatformVersion = update.maxPlatformVersion();
        }
        if (update.supportedPlatforms() != null) {
            entity.supportedPlatforms = listToJson(update.supportedPlatforms());
        }
        if (update.permissionsRequired() != null) {
            entity.permissionsRequired = listToJson(update.permissionsRequired());
        }
        if (update.configSchema() != null) {
            entity.configSchema = update.configSchema();
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
}