package com.easystation.plugin.loader;

import com.easystation.plugin.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PluginRegistry 单元测试
 */
class PluginRegistryTest {
    
    private PluginRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new PluginRegistry();
    }
    
    @Test
    void testRegisterAndGetPlugin() {
        // 创建模拟插件
        MockPlugin plugin = new MockPlugin("test-plugin", "1.0.0");
        
        // 注册
        registry.register(plugin);
        
        // 验证
        assertTrue(registry.isRegistered("test-plugin"));
        assertEquals(1, registry.size());
        
        // 获取
        var result = registry.getPlugin("test-plugin");
        assertTrue(result.isPresent());
        assertEquals("test-plugin", result.get().getDescriptor().getId());
    }
    
    @Test
    void testRegisterDuplicate() {
        MockPlugin plugin = new MockPlugin("test-plugin", "1.0.0");
        registry.register(plugin);
        
        // 重复注册应抛出异常
        assertThrows(IllegalStateException.class, () -> {
            registry.register(plugin);
        });
    }
    
    @Test
    void testUnregister() {
        MockPlugin plugin = new MockPlugin("test-plugin", "1.0.0");
        registry.register(plugin);
        
        // 注销
        var unregistered = registry.unregister("test-plugin");
        
        assertNotNull(unregistered);
        assertFalse(registry.isRegistered("test-plugin"));
        assertEquals(0, registry.size());
    }
    
    @Test
    void testGetAllPlugins() {
        registry.register(new MockPlugin("plugin-1", "1.0.0"));
        registry.register(new MockPlugin("plugin-2", "2.0.0"));
        registry.register(new MockPlugin("plugin-3", "3.0.0"));
        
        List<Plugin> all = registry.getAllPlugins();
        
        assertEquals(3, all.size());
    }
    
    @Test
    void testGetPluginsByState() {
        MockPlugin activePlugin = new MockPlugin("active-plugin", "1.0.0");
        activePlugin.setState(PluginState.ACTIVE);
        
        MockPlugin stoppedPlugin = new MockPlugin("stopped-plugin", "1.0.0");
        stoppedPlugin.setState(PluginState.STOPPED);
        
        registry.register(activePlugin);
        registry.register(stoppedPlugin);
        
        List<Plugin> activePlugins = registry.getPluginsByState(PluginState.ACTIVE);
        
        assertEquals(1, activePlugins.size());
        assertEquals("active-plugin", activePlugins.get(0).getDescriptor().getId());
    }
    
    @Test
    void testClear() {
        registry.register(new MockPlugin("plugin-1", "1.0.0"));
        registry.register(new MockPlugin("plugin-2", "2.0.0"));
        
        registry.clear();
        
        assertEquals(0, registry.size());
        assertTrue(registry.getAllPlugins().isEmpty());
    }
    
    /**
     * 模拟插件用于测试
     */
    static class MockPlugin implements Plugin {
        private final MockDescriptor descriptor;
        private PluginState state = PluginState.NEW;
        
        MockPlugin(String id, String version) {
            this.descriptor = new MockDescriptor(id, version);
        }
        
        void setState(PluginState state) {
            this.state = state;
        }
        
        @Override
        public PluginDescriptor getDescriptor() {
            return descriptor;
        }
        
        @Override
        public void initialize(PluginContext context) throws PluginException {
            state = PluginState.INITIALIZED;
        }
        
        @Override
        public void start() throws PluginException {
            state = PluginState.ACTIVE;
        }
        
        @Override
        public void stop() {
            state = PluginState.STOPPED;
        }
        
        @Override
        public PluginState getState() {
            return state;
        }
    }
    
    /**
     * 模拟描述符用于测试
     */
    static class MockDescriptor implements PluginDescriptor {
        private final String id;
        private final String version;
        
        MockDescriptor(String id, String version) {
            this.id = id;
            this.version = version;
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getName() {
            return "Mock Plugin " + id;
        }
        
        @Override
        public String getVersion() {
            return version;
        }
        
        @Override
        public String getMainClass() {
            return "com.example.MockPlugin";
        }
        
        // 其他方法返回 null 或空列表
        @Override
        public String getDescription() { return null; }
        @Override
        public String getAuthor() { return null; }
        @Override
        public String getLicense() { return null; }
        @Override
        public java.util.List<PluginDependency> getDependencies() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<String> getProvides() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<String> getRequires() { return java.util.Collections.emptyList(); }
        @Override
        public java.util.Map<String, Object> getConfigDefaults() { return java.util.Collections.emptyMap(); }
        @Override
        public java.util.Map<String, Object> getConfigSchema() { return java.util.Collections.emptyMap(); }
    }
}
