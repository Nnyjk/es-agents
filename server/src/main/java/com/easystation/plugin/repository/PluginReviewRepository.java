package com.easystation.plugin.repository;

import com.easystation.plugin.domain.entity.PluginReview;
import com.easystation.plugin.domain.enums.ReviewStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginReviewRepository implements PanacheRepositoryBase<PluginReview, UUID> {

    public List<PluginReview> findByPluginId(UUID pluginId) {
        return list("plugin.id", pluginId);
    }

    public List<PluginReview> findByPluginIdOrderByCreatedAtDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginReview> findByVersionId(UUID versionId) {
        return list("version.id", versionId);
    }

    public List<PluginReview> findByReviewerId(UUID reviewerId) {
        return list("reviewer.id", reviewerId);
    }

    public List<PluginReview> findByStatus(ReviewStatus status) {
        return list("status", status);
    }

    public List<PluginReview> findByPluginIdAndStatus(UUID pluginId, ReviewStatus status) {
        return list("plugin.id = ?1 and status = ?2", pluginId, status);
    }

    public Optional<PluginReview> findLatestByPluginId(UUID pluginId) {
        return find("plugin.id = ?1 ORDER BY createdAt DESC", pluginId).firstResultOptional();
    }

    public Optional<PluginReview> findPendingByPluginId(UUID pluginId) {
        return find("plugin.id = ?1 and status = ?2", pluginId, ReviewStatus.PENDING).firstResultOptional();
    }

    public long countByPluginId(UUID pluginId) {
        return count("plugin.id", pluginId);
    }

    public long countByStatus(ReviewStatus status) {
        return count("status", status);
    }

    public long countByReviewerId(UUID reviewerId) {
        return count("reviewer.id", reviewerId);
    }

    public boolean existsByPluginIdAndStatus(UUID pluginId, ReviewStatus status) {
        return count("plugin.id = ?1 and status = ?2", pluginId, status) > 0;
    }
}