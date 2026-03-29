package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginRecord;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.easystation.plugin.domain.enums.PluginStatus;
import com.easystation.plugin.domain.enums.ReviewStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PluginServiceTest {

    @Inject
    PluginService pluginService;

    private UUID testDeveloperId;
    private String testDeveloperName;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testDeveloperId = UUID.randomUUID();
        testDeveloperName = "test-developer";
        testCategoryId = UUID.randomUUID();
    }

    @Test
    void testCreatePlugin() {
        PluginRecord.Create create = new PluginRecord.Create(
            "test-plugin-" + System.currentTimeMillis(),
            "测试插件",
            "这是一个测试插件",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            "https://example.com/icon.png",
            List.of("测试", "工具")
        );

        PluginRecord result = pluginService.create(create);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("test-plugin-" + System.currentTimeMillis(), result.code());
        assertEquals(PluginStatus.DRAFT, result.status());
    }

    @Test
    void testFindById() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "find-by-id-test",
            "查找测试",
            "用于测试查找功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);

        // Then find it
        Optional<PluginRecord> found = pluginService.findById(created.id());

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("find-by-id-test", found.get().code());
    }

    @Test
    void testFindByCode() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "find-by-code-test",
            "代码查找测试",
            "用于测试代码查找功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);

        // Then find it by code
        Optional<PluginRecord> found = pluginService.findByCode("find-by-code-test");

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("find-by-code-test", found.get().code());
    }

    @Test
    void testSearch() {
        // Create test plugins
        PluginRecord.Create create1 = new PluginRecord.Create(
            "search-test-1",
            "搜索测试插件一",
            "用于测试搜索功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of("搜索", "测试")
        );
        pluginService.create(create1);

        PluginRecord.Create create2 = new PluginRecord.Create(
            "search-test-2",
            "搜索测试插件二",
            "用于测试搜索功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of("搜索", "工具")
        );
        pluginService.create(create2);

        // Search by keyword
        PluginRecord.Query query = new PluginRecord.Query("搜索", null, null, null, null, null, null);
        List<PluginRecord> results = pluginService.search(query);

        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }

    @Test
    void testUpdate() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "update-test",
            "更新前名称",
            "更新前描述",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);

        // Update the plugin
        PluginRecord.Update update = new PluginRecord.Update(
            "更新后名称",
            "更新后描述",
            "https://example.com/new-icon.png",
            List.of("更新", "测试")
        );
        PluginRecord updated = pluginService.update(created.id(), update);

        assertNotNull(updated);
        assertEquals("更新后名称", updated.name());
        assertEquals("更新后描述", updated.description());
        assertEquals("https://example.com/new-icon.png", updated.iconUrl());
    }

    @Test
    void testPublish() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "publish-test",
            "发布测试",
            "用于测试发布功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);

        // Publish the plugin
        PluginRecord published = pluginService.publish(created.id());

        assertNotNull(published);
        assertEquals(PluginStatus.PENDING_REVIEW, published.status());
    }

    @Test
    void testSuspend() {
        // First create and publish a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "suspend-test",
            "暂停测试",
            "用于测试暂停功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);
        pluginService.publish(created.id());

        // Suspend the plugin
        PluginRecord suspended = pluginService.suspend(created.id(), "违反社区规范");

        assertNotNull(suspended);
        assertEquals(PluginStatus.SUSPENDED, suspended.status());
        assertNotNull(suspended.suspendReason());
    }

    @Test
    void testDelete() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "delete-test",
            "删除测试",
            "用于测试删除功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord created = pluginService.create(create);

        // Delete the plugin
        PluginRecord deleted = pluginService.delete(created.id());

        assertNotNull(deleted);
        assertEquals(PluginStatus.DELETED, deleted.status());
    }

    @Test
    void testCreateVersion() {
        // First create a plugin
        PluginRecord.Create create = new PluginRecord.Create(
            "version-test",
            "版本测试",
            "用于测试版本功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord plugin = pluginService.create(create);

        // Create a version
        PluginVersionRecord.CreateVersion createVersion = new PluginVersionRecord.CreateVersion(
            plugin.id(),
            "1.0.0",
            "初始版本",
            "https://example.com/download/v1.0.0.zip",
            "SHA256_HASH_VALUE",
            1024000L,
            LocalDateTime.now()
        );
        PluginVersionRecord version = pluginService.createVersion(createVersion);

        assertNotNull(version);
        assertNotNull(version.id());
        assertEquals("1.0.0", version.version());
        assertEquals(plugin.id(), version.pluginId());
    }

    @Test
    void testFindLatestVersion() {
        // First create a plugin and versions
        PluginRecord.Create create = new PluginRecord.Create(
            "latest-version-test",
            "最新版本测试",
            "用于测试最新版本功能",
            testCategoryId,
            testDeveloperId,
            testDeveloperName,
            "1.0.0",
            null,
            List.of()
        );
        PluginRecord plugin = pluginService.create(create);

        PluginVersionRecord.CreateVersion createVersion1 = new PluginVersionRecord.CreateVersion(
            plugin.id(),
            "1.0.0",
            "初始版本",
            "https://example.com/download/v1.0.0.zip",
            "HASH1",
            1024000L,
            LocalDateTime.now().minusDays(10)
        );
        pluginService.createVersion(createVersion1);

        PluginVersionRecord.CreateVersion createVersion2 = new PluginVersionRecord.CreateVersion(
            plugin.id(),
            "1.1.0",
            "功能更新",
            "https://example.com/download/v1.1.0.zip",
            "HASH2",
            1048576L,
            LocalDateTime.now()
        );
        pluginService.createVersion(createVersion2);

        // Find latest version
        Optional<PluginVersionRecord> latest = pluginService.findLatestVersion(plugin.id());

        assertTrue(latest.isPresent());
        assertEquals("1.1.0", latest.get().version());
    }
}
