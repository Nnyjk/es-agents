package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginCategoryRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginCategoryService {

    PluginCategoryRecord create(PluginCategoryRecord.Create create);

    PluginCategoryRecord update(UUID id, PluginCategoryRecord.Update update);

    Optional<PluginCategoryRecord> findById(UUID id);

    Optional<PluginCategoryRecord> findByCode(String code);

    List<PluginCategoryRecord> findAll();

    List<PluginCategoryRecord> findRootCategories();

    List<PluginCategoryRecord> findChildren(UUID parentId);

    List<PluginCategoryRecord.Tree> getCategoryTree();

    PluginCategoryRecord activate(UUID id);

    PluginCategoryRecord deactivate(UUID id);

    void delete(UUID id);

    long countPlugins(UUID categoryId);
}