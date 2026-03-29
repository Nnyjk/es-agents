package com.easystation.plugin.marketplace.domain;

import com.easystation.plugin.marketplace.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 插件市场服务测试
 */
class PluginMarketplaceTest {
    
    private PluginMarketplace marketplace;
    
    @BeforeEach
    void setUp() {
        marketplace = new PluginMarketplaceImpl();
    }
    
    @Test
    @DisplayName("测试获取插件列表")
    void testListPlugins() {
        PluginListResponse response = marketplace.listPlugins(1, 10);
        
        assertNotNull(response);
        assertTrue(response.total() > 0);
        assertEquals(1, response.page());
        assertEquals(10, response.pageSize());
        assertFalse(response.plugins().isEmpty());
    }
    
    @Test
    @DisplayName("测试获取插件详情")
    void testGetPluginDetail() {
        PluginDetailResponse response = marketplace.getPluginDetail("code-formatter");
        
        assertNotNull(response);
        assertEquals("code-formatter", response.id());
        assertEquals("Code Formatter", response.name());
    }
    
    @Test
    @DisplayName("测试插件详情 - 未找到")
    void testGetPluginDetailNotFound() {
        assertThrows(
            PluginMarketplace.PluginNotFoundException.class,
            () -> marketplace.getPluginDetail("non-existent-plugin")
        );
    }
    
    @Test
    @DisplayName("测试搜索插件 - 按关键词")
    void testSearchPluginsByKeyword() {
        PluginSearchRequest request = new PluginSearchRequest("code", null, null, 1, 10, "name", "asc");
        PluginListResponse response = marketplace.searchPlugins(request);
        
        assertNotNull(response);
        assertFalse(response.plugins().isEmpty());
    }
    
    @Test
    @DisplayName("测试搜索插件 - 按分类")
    void testSearchPluginsByCategory() {
        PluginSearchRequest request = new PluginSearchRequest(null, "development", null, 1, 10, "name", "asc");
        PluginListResponse response = marketplace.searchPlugins(request);
        
        assertNotNull(response);
        assertTrue(response.plugins().stream()
            .allMatch(p -> "development".equals(p.category())));
    }
    
    @Test
    @DisplayName("测试安装插件")
    void testInstallPlugin() {
        PluginInstallRequest request = new PluginInstallRequest("code-formatter", "1.0.0");
        
        assertDoesNotThrow(() -> marketplace.installPlugin(request));
    }
    
    @Test
    @DisplayName("测试安装插件 - 未找到")
    void testInstallPluginNotFound() {
        PluginInstallRequest request = new PluginInstallRequest("non-existent", "1.0.0");
        
        assertThrows(
            PluginMarketplace.PluginNotFoundException.class,
            () -> marketplace.installPlugin(request)
        );
    }
    
    @Test
    @DisplayName("测试卸载插件")
    void testUninstallPlugin() {
        assertDoesNotThrow(() -> marketplace.uninstallPlugin("code-formatter"));
    }
    
    @Test
    @DisplayName("测试评分插件")
    void testRatePlugin() {
        assertDoesNotThrow(() -> marketplace.ratePlugin("code-formatter", 5, "Great plugin!"));
    }
    
    @Test
    @DisplayName("测试评分插件 - 无效评分")
    void testRatePluginInvalidRating() {
        assertThrows(
            IllegalArgumentException.class,
            () -> marketplace.ratePlugin("code-formatter", 6, "Invalid rating")
        );
    }
}
