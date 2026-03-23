package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginComment;
import com.easystation.plugin.dto.PluginCommentRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginCommentMapper {

    @Mapping(target = "pluginId", source = "plugin.id")
    @Mapping(target = "pluginName", source = "plugin.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatar")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "replyToUserId", source = "replyToUser.id")
    @Mapping(target = "replyToUserName", source = "replyToUser.username")
    @Mapping(target = "replies", ignore = true)
    PluginCommentRecord toRecord(PluginComment entity);

    List<PluginCommentRecord> toRecords(List<PluginComment> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plugin", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replyToUser", ignore = true)
    @Mapping(target = "isDeveloperReply", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "isHidden", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "replies", ignore = true)
    void updateEntity(PluginCommentRecord.Create dto, @MappingTarget PluginComment entity);

    default PluginComment fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginComment comment = new PluginComment();
        comment.setId(id);
        return comment;
    }
}