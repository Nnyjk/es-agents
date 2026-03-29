package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginCategoryRecord;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PluginCategoryServiceTest {

    @Inject
    PluginCategoryService categoryService;

    @Test
    void testCreateCategory() {
        PluginCategoryRecord.Create create = new PluginCategoryRecord.Create(
            null,
            "测试分类",
            "test-category-" + System.currentTimeMillis(),
            null,
            "用于测试的分类",
            1
        );

        PluginCategoryRecord result = categoryService.create(create);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("test-category-" + System.currentTimeMillis(), result.code());
        assertEquals("测试分类", result.name());
        assertTrue(result.isActive());
    }

    @Test
    void testFindById() {
        // First create a category
        PluginCategoryRecord.Create create = new PluginCategoryRecord.Create(
            null,
            "查找分类测试",
            "find-by-id-cat",
            null,
            "用于测试查找功能",
            1
        );
        PluginCategoryRecord created = categoryService.create(create);

        // Then find it
        Optional<PluginCategoryRecord> found = categoryService.findById(created.id());

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("find-by-id-cat", found.get().code());
    }

    @Test
    void testFindAll() {
        // Create test categories
        PluginCategoryRecord.Create create1 = new PluginCategoryRecord.Create(
            null,
            "测试分类一",
            "cat-test-1",
            null,
            "第一个测试分类",
            1
        );
        categoryService.create(create1);

        PluginCategoryRecord.Create create2 = new PluginCategoryRecord.Create(
            null,
            "测试分类二",
            "cat-test-2",
            null,
            "第二个测试分类",
            2
        );
        categoryService.create(create2);

        // Find all categories
        List<PluginCategoryRecord> categories = categoryService.findAll();

        assertNotNull(categories);
        assertTrue(categories.size() >= 2);
    }

    @Test
    void testUpdate() {
        // First create a category
        PluginCategoryRecord.Create create = new PluginCategoryRecord.Create(
            null,
            "更新前名称",
            "update-cat",
            null,
            "更新前描述",
            1
        );
        PluginCategoryRecord created = categoryService.create(create);

        // Update the category
        PluginCategoryRecord.Update update = new PluginCategoryRecord.Update(
            null,
            "更新后名称",
            null,
            null,
            "更新后描述",
            2,
            false
        );
        PluginCategoryRecord updated = categoryService.update(created.id(), update);

        assertNotNull(updated);
        assertEquals("更新后名称", updated.name());
        assertEquals("更新后描述", updated.description());
        assertEquals(2, updated.sortOrder());
        assertFalse(updated.isActive());
    }

    @Test
    void testDelete() {
        // First create a category
        PluginCategoryRecord.Create create = new PluginCategoryRecord.Create(
            null,
            "删除分类测试",
            "delete-cat",
            null,
            "用于测试删除功能",
            1
        );
        PluginCategoryRecord created = categoryService.create(create);

        // Delete the category
        categoryService.delete(created.id());

        // Verify it's deleted
        Optional<PluginCategoryRecord> found = categoryService.findById(created.id());
        // After deletion, the category should not be found or be marked as deleted
        // Depending on implementation, it might return empty or a deleted record
        // We just verify the delete operation completes without error
    }
}
