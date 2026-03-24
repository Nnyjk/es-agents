package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.PluginCategory;
import com.easystation.plugin.dto.PluginCategoryRecord;
import com.easystation.plugin.mapper.PluginCategoryMapper;
import com.easystation.plugin.repository.PluginCategoryRepository;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.service.PluginCategoryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginCategoryServiceImpl implements PluginCategoryService {

    @Inject
    PluginCategoryRepository categoryRepository;

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginCategoryMapper categoryMapper;

    @Override
    @Transactional
    public PluginCategoryRecord create(PluginCategoryRecord.Create create) {
        // Check if name already exists under the same parent
        if (categoryRepository.existsByNameAndParentId(create.name(), create.parentId())) {
            throw new BadRequestException("Category name already exists under this parent");
        }

        // Check if code already exists (if provided)
        if (create.code() != null && categoryRepository.existsByCode(create.code())) {
            throw new BadRequestException("Category code already exists: " + create.code());
        }

        PluginCategory category = new PluginCategory();
        category.name = create.name();
        category.code = create.code() != null ? create.code() : generateCode(create.name());
        
        if (create.parentId() != null) {
            PluginCategory parent = categoryRepository.findByIdOptional(create.parentId())
                .orElseThrow(() -> new NotFoundException("Parent category not found: " + create.parentId()));
            category.parentId = parent.id;
        }
        
        category.icon = create.icon();
        category.description = create.description();
        category.sortOrder = create.sortOrder() != null ? create.sortOrder() : 0;
        category.isActive = true;

        categoryRepository.persist(category);
        return categoryMapper.toRecord(category);
    }

    @Override
    @Transactional
    public PluginCategoryRecord update(UUID id, PluginCategoryRecord.Update update) {
        PluginCategory category = categoryRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        if (update.name() != null) {
            // Check if name already exists under the same parent
            if (!update.name().equals(category.name) && 
                categoryRepository.existsByNameAndParentId(update.name(), category.parentId)) {
                throw new BadRequestException("Category name already exists under this parent");
            }
            category.name = update.name();
        }

        if (update.code() != null) {
            if (!update.code().equals(category.code) && 
                categoryRepository.existsByCode(update.code())) {
                throw new BadRequestException("Category code already exists: " + update.code());
            }
            category.code = update.code();
        }

        if (update.parentId() != null) {
            // Prevent setting self as parent
            if (update.parentId().equals(category.id)) {
                throw new BadRequestException("Cannot set self as parent");
            }
            category.parentId = update.parentId();
        }
        if (update.icon() != null) {
            category.icon = update.icon();
        }
        if (update.description() != null) {
            category.description = update.description();
        }
        if (update.sortOrder() != null) {
            category.sortOrder = update.sortOrder();
        }
        if (update.isActive() != null) {
            category.isActive = update.isActive();
        }

        categoryRepository.persist(category);
        return categoryMapper.toRecord(category);
    }

    @Override
    public Optional<PluginCategoryRecord> findById(UUID id) {
        return categoryRepository.findByIdOptional(id)
            .map(categoryMapper::toRecord);
    }

    @Override
    public Optional<PluginCategoryRecord> findByCode(String code) {
        return categoryRepository.findByCode(code)
            .map(categoryMapper::toRecord);
    }

    @Override
    public List<PluginCategoryRecord> findAll() {
        return categoryRepository.findAll().list().stream()
            .map(categoryMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCategoryRecord> findRootCategories() {
        return categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder().stream()
            .map(categoryMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCategoryRecord> findChildren(UUID parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(parentId).stream()
            .map(categoryMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCategoryRecord.Tree> getCategoryTree() {
        List<PluginCategory> rootCategories = categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
        return rootCategories.stream()
            .map(this::buildTree)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PluginCategoryRecord activate(UUID id) {
        PluginCategory category = categoryRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        category.isActive = true;

        categoryRepository.persist(category);
        return categoryMapper.toRecord(category);
    }

    @Override
    @Transactional
    public PluginCategoryRecord deactivate(UUID id) {
        PluginCategory category = categoryRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        category.isActive = false;

        categoryRepository.persist(category);
        return categoryMapper.toRecord(category);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PluginCategory category = categoryRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        // Check if category has children
        if (categoryRepository.countByParentId(id) > 0) {
            throw new BadRequestException("Cannot delete category with children");
        }

        // Check if category has plugins
        if (pluginRepository.countByCategoryId(id) > 0) {
            throw new BadRequestException("Cannot delete category with plugins");
        }

        categoryRepository.delete(category);
    }

    @Override
    public long countPlugins(UUID categoryId) {
        return pluginRepository.countByCategoryId(categoryId);
    }

    private String generateCode(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    private PluginCategoryRecord.Tree buildTree(PluginCategory category) {
        List<PluginCategoryRecord.Tree> children = categoryRepository
            .findByParentIdAndIsActiveTrueOrderBySortOrder(category.id)
            .stream()
            .map(this::buildTree)
            .collect(Collectors.toList());

        return new PluginCategoryRecord.Tree(
            category.id,
            category.name,
            category.code,
            category.icon,
            pluginRepository.countByCategoryId(category.id),
            children
        );
    }
}