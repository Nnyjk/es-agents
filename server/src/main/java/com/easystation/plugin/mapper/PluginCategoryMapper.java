package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginCategory;
import com.easystation.plugin.dto.PluginCategoryRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginCategoryMapper {

    public PluginCategoryRecord toRecord(PluginCategory entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginCategoryRecord(
            entity.id,
            entity.parentId,
            entity.name,
            entity.code,
            entity.icon,
            entity.description,
            entity.sortOrder,
            entity.isActive,
            null, // pluginCount - needs to be set by service
            entity.createdAt,
            entity.updatedAt,
            Collections.emptyList() // children - needs to be set by service
        );
    }

    public List<PluginCategoryRecord> toRecords(List<PluginCategory> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginCategory toEntity(PluginCategoryRecord.Create create) {
        if (create == null) {
            return null;
        }
        
        PluginCategory entity = new PluginCategory();
        entity.parentId = create.parentId();
        entity.name = create.name();
        entity.code = create.code();
        entity.icon = create.icon();
        entity.description = create.description();
        entity.sortOrder = create.sortOrder() != null ? create.sortOrder() : 0;
        entity.isActive = true;
        
        return entity;
    }

    public void updateEntity(PluginCategory entity, PluginCategoryRecord.Update update) {
        if (entity == null || update == null) {
            return;
        }
        
        if (update.parentId() != null) {
            entity.parentId = update.parentId();
        }
        if (update.name() != null) {
            entity.name = update.name();
        }
        if (update.code() != null) {
            entity.code = update.code();
        }
        if (update.icon() != null) {
            entity.icon = update.icon();
        }
        if (update.description() != null) {
            entity.description = update.description();
        }
        if (update.sortOrder() != null) {
            entity.sortOrder = update.sortOrder();
        }
        if (update.isActive() != null) {
            entity.isActive = update.isActive();
        }
    }
}