package com.easystation.plugin.repository;

import com.easystation.plugin.domain.PluginCategory;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginCategoryRepository implements PanacheRepositoryBase<PluginCategory, UUID> {

    public Optional<PluginCategory> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public List<PluginCategory> findByParentId(UUID parentId) {
        return list("parent.id", parentId);
    }

    public List<PluginCategory> findByParentIdIsNull() {
        return list("parent IS NULL");
    }

    public List<PluginCategory> findByIsActiveTrue() {
        return list("isActive", true);
    }

    public List<PluginCategory> findByParentIdIsNullAndIsActiveTrueOrderBySortOrder() {
        return list("parent IS NULL and isActive = true ORDER BY sortOrder");
    }

    public List<PluginCategory> findByParentIdAndIsActiveTrueOrderBySortOrder(UUID parentId) {
        return list("parent.id = ?1 and isActive = true ORDER BY sortOrder", parentId);
    }

    public long countByParentId(UUID parentId) {
        return count("parent.id", parentId);
    }

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByNameAndParentId(String name, UUID parentId) {
        if (parentId == null) {
            return count("name = ?1 and parent IS NULL", name) > 0;
        }
        return count("name = ?1 and parent.id = ?2", name, parentId) > 0;
    }
}