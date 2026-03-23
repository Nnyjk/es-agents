package com.easystation.plugin.repository;

import com.easystation.plugin.domain.entity.PluginComment;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginCommentRepository implements PanacheRepositoryBase<PluginComment, UUID> {

    public List<PluginComment> findByPluginId(UUID pluginId) {
        return list("plugin.id", pluginId);
    }

    public List<PluginComment> findByPluginIdOrderByCreatedAtDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdOrderByLikeCountDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY likeCount DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdAndParentIdIsNull(UUID pluginId) {
        return list("plugin.id = ?1 and parent IS NULL ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdAndParentIdIsNullOrderByPinnedAndCreatedAt(UUID pluginId) {
        return list("plugin.id = ?1 and parent IS NULL ORDER BY isPinned DESC, createdAt DESC", pluginId);
    }

    public List<PluginComment> findByParentId(UUID parentId) {
        return list("parent.id", parentId);
    }

    public List<PluginComment> findByParentIdOrderByCreatedAtAsc(UUID parentId) {
        return list("parent.id = ?1 ORDER BY createdAt ASC", parentId);
    }

    public List<PluginComment> findByUserId(UUID userId) {
        return list("user.id", userId);
    }

    public List<PluginComment> findByPluginIdAndIsDeveloperReplyTrue(UUID pluginId) {
        return list("plugin.id = ?1 and isDeveloperReply = true", pluginId);
    }

    public List<PluginComment> findByPluginIdAndIsHiddenTrue(UUID pluginId) {
        return list("plugin.id = ?1 and isHidden = true", pluginId);
    }

    public long countByPluginId(UUID pluginId) {
        return count("plugin.id", pluginId);
    }

    public long countByUserId(UUID userId) {
        return count("user.id", userId);
    }

    public long countByParentId(UUID parentId) {
        return count("parent.id", parentId);
    }

    public long countByPluginIdAndIsPinnedTrue(UUID pluginId) {
        return count("plugin.id = ?1 and isPinned = true", pluginId);
    }

    public long countByPluginIdAndIsHiddenTrue(UUID pluginId) {
        return count("plugin.id = ?1 and isHidden = true", pluginId);
    }

    public Optional<PluginComment> findByIdAndPluginId(UUID id, UUID pluginId) {
        return find("id = ?1 and plugin.id = ?2", id, pluginId).firstResultOptional();
    }
}