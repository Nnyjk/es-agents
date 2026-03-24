package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginRating;
import com.easystation.plugin.dto.PluginRatingRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginRatingMapper {

    public PluginRatingRecord toRecord(PluginRating entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginRatingRecord(
            entity.id,
            entity.pluginId,
            null, // pluginName - needs to be set by service
            entity.userId,
            null, // userName - needs to be set by service
            entity.rating,
            entity.review,
            entity.isVerified,
            entity.helpfulCount,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public List<PluginRatingRecord> toRecords(List<PluginRating> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginRating toEntity(PluginRatingRecord.Create create, UUID userId) {
        if (create == null) {
            return null;
        }
        
        PluginRating entity = new PluginRating();
        entity.pluginId = create.pluginId();
        entity.userId = userId;
        entity.rating = create.rating();
        entity.review = create.review();
        entity.isVerified = false;
        entity.helpfulCount = 0;
        
        return entity;
    }

    public void updateEntity(PluginRating entity, PluginRatingRecord.Update update) {
        if (entity == null || update == null) {
            return;
        }
        
        if (update.rating() != null) {
            entity.rating = update.rating();
        }
        if (update.review() != null) {
            entity.review = update.review();
        }
    }
}