package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginComment;
import com.easystation.plugin.dto.PluginCommentRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginCommentMapper {

    public PluginCommentRecord toRecord(PluginComment entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginCommentRecord(
            entity.id,
            entity.pluginId,
            null, // pluginName - needs to be set by service
            entity.userId,
            null, // userName - needs to be set by service
            null, // userAvatar - needs to be set by service
            entity.parentId,
            entity.replyToUserId,
            null, // replyToUserName - needs to be set by service
            entity.content,
            entity.isDeveloperReply,
            entity.isPinned,
            entity.isHidden,
            entity.likeCount,
            entity.replyCount,
            entity.createdAt,
            entity.updatedAt,
            Collections.emptyList() // replies - needs to be set by service
        );
    }

    public List<PluginCommentRecord> toRecords(List<PluginComment> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginComment toEntity(PluginCommentRecord.Create create, UUID userId) {
        if (create == null) {
            return null;
        }
        
        PluginComment entity = new PluginComment();
        entity.pluginId = create.pluginId();
        entity.userId = userId;
        entity.parentId = create.parentId();
        entity.replyToUserId = create.replyToUserId();
        entity.content = create.content();
        entity.isDeveloperReply = false;
        entity.isPinned = false;
        entity.isHidden = false;
        entity.likeCount = 0;
        entity.replyCount = 0;
        
        return entity;
    }

    public void updateEntity(PluginComment entity, PluginCommentRecord.Update update) {
        if (entity == null || update == null) {
            return;
        }
        
        if (update.content() != null) {
            entity.content = update.content();
        }
    }
}