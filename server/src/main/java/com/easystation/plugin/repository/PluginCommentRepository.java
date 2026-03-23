package com.easystation.plugin.repository;

import com.easystation.plugin.domain.PluginComment;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginCommentRepository implements PanacheRepositoryBase<PluginComment, UUID> {

    public List<PluginComment> findByPluginId(UUID pluginId) {
        return list("pluginId", pluginId);
    }

    public List<PluginComment> findByPluginIdOrderByCreatedAtDesc(UUID pluginId) {
        return list("pluginId = ?1 ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdOrderByLikeCountDesc(UUID pluginId) {
        return list("pluginId = ?1 ORDER BY likeCount DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdAndParentIdIsNull(UUID pluginId) {
        return list("pluginId = ?1 and parentId IS NULL ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginComment> findByPluginIdAndParentIdIsNullOrderByLikeCountDesc(UUID pluginId) {
        return list("pluginId = ?1 and parentId IS NULL ORDER BY likeCount DESC, createdAt DESC", pluginId);
    }

    public List<PluginComment> findByParentId(UUID parentId) {
        return list("parentId = ?1 ORDER BY createdAt ASC", parentId);
    }

    public List<PluginComment> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    public Optional<PluginComment> findByIdAndPluginId(UUID id, UUID pluginId) {
        return find("id = ?1 and pluginId = ?2", id, pluginId).firstResultOptional();
    }

    public long countByPluginId(UUID pluginId) {
        return count("pluginId", pluginId);
    }

    public long countByUserId(UUID userId) {
        return count("userId", userId);
    }

    public long countByParentId(UUID parentId) {
        return count("parentId", parentId);
    }

    public long countByPluginIdAndIsPinnedTrue(UUID pluginId) {
        return count("pluginId = ?1 and isPinned = true", pluginId);
    }

    public long countByPluginIdAndIsHiddenTrue(UUID pluginId) {
        return count("pluginId = ?1 and isHidden = true", pluginId);
    }

    public List<PluginComment> findPinnedByPluginId(UUID pluginId) {
        return list("pluginId = ?1 and isPinned = true ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginComment> findDeveloperRepliesByPluginId(UUID pluginId) {
        return list("pluginId = ?1 and isDeveloperReply = true ORDER BY createdAt DESC", pluginId);
    }
}