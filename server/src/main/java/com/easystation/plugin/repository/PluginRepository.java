package com.easystation.plugin.repository;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.enums.PluginStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginRepository implements PanacheRepositoryBase<Plugin, UUID> {

    public Optional<Plugin> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public List<Plugin> findByDeveloperId(UUID developerId) {
        return list("developer.id", developerId);
    }

    public List<Plugin> findByCategoryId(UUID categoryId) {
        return list("category.id", categoryId);
    }

    public List<Plugin> findByStatus(PluginStatus status) {
        return list("status", status);
    }

    public List<Plugin> findByDeveloperIdAndStatus(UUID developerId, PluginStatus status) {
        return list("developer.id = ?1 and status = ?2", developerId, status);
    }

    public List<Plugin> findByCategoryIdAndStatus(UUID categoryId, PluginStatus status) {
        return list("category.id = ?1 and status = ?2", categoryId, status);
    }

    public List<Plugin> findByNameContainingIgnoreCase(String name) {
        return list("LOWER(name) LIKE LOWER(CONCAT('%', ?1, '%'))", name);
    }

    public List<Plugin> findByTag(String tag) {
        return list("tags LIKE CONCAT('%', ?1, '%')", tag);
    }

    public List<Plugin> findByIsFree(Boolean isFree) {
        return list("isFree", isFree);
    }

    public List<Plugin> findByStatusOrderByPublishedAtDesc(PluginStatus status) {
        return list("status = ?1 ORDER BY publishedAt DESC", status);
    }

    public List<Plugin> findByStatusOrderByTotalDownloadsDesc(PluginStatus status) {
        return list("status = ?1 ORDER BY totalDownloads DESC", status);
    }

    public List<Plugin> findByStatusOrderByAverageRatingDesc(PluginStatus status) {
        return list("status = ?1 ORDER BY averageRating DESC", status);
    }

    public long countByStatus(PluginStatus status) {
        return count("status", status);
    }

    public long countByDeveloperId(UUID developerId) {
        return count("developer.id", developerId);
    }

    public long countByCategoryId(UUID categoryId) {
        return count("category.id", categoryId);
    }

    public long countByIsFree(Boolean isFree) {
        return count("isFree", isFree);
    }

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByNameAndDeveloperId(String name, UUID developerId) {
        return count("name = ?1 and developer.id = ?2", name, developerId) > 0;
    }
}