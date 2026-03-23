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
        return list("parentId", parentId);
    }

    public List<PluginCategory> findByParentIdIsNull() {
        return list("parentId IS NULL");
    }

    public List<PluginCategory> findByIsActiveTrue() {
        return list("isActive", true);
    }

    public List<PluginCategory> findByParentIdIsNullAndIsActiveTrueOrderBySortOrder() {
        return list("parentId IS NULL and isActive = true ORDER BY sortOrder, name");
    }

    public List<PluginCategory> findByParentIdAndIsActiveTrueOrderBySortOrder(UUID parentId) {
        return list("parentId = ?1 and isActive = true ORDER BY sortOrder, name", parentId);
    }

    public long countByParentId(UUID parentId) {
        return count("parentId", parentId);
    }

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByNameAndParentId(String name, UUID parentId) {
        if (parentId == null) {
            return count("name = ?1 and parentId IS NULL", name) > 0;
        }
        return count("name = ?1 and parentId = ?2", name, parentId) > 0;
    }

    public List<PluginCategory> findAllOrderBySortOrder() {
        return list("ORDER BY sortOrder, name");
    }

    public List<PluginCategory> findActiveCategoriesOrderBySortOrder() {
        return list("isActive = true ORDER BY sortOrder, name");
    }
}