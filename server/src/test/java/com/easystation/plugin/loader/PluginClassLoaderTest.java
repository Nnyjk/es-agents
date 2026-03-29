package com.easystation.plugin.loader;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PluginClassLoader 单元测试
 */
class PluginClassLoaderTest {
    
    @Test
    void testConstructor() {
        URL[] urls = new URL[0];
        ClassLoader parent = getClass().getClassLoader();
        
        PluginClassLoader classLoader = new PluginClassLoader("test-plugin", urls, parent);
        
        assertEquals("test-plugin", classLoader.getPluginId());
    }
    
    @Test
    void testIsSystemClass() throws Exception {
        URL[] urls = new URL[0];
        ClassLoader parent = getClass().getClassLoader();
        
        PluginClassLoader classLoader = new PluginClassLoader("test-plugin", urls, parent);
        
        // 系统类应该由父加载器加载
        assertTrue(classLoader.loadClass("java.lang.String") != null);
        assertTrue(classLoader.loadClass("java.util.List") != null);
        
        // 插件 API 类也应该由父加载器加载
        // 注意：如果插件 API 不在 classpath 中，这里会抛出 ClassNotFoundException
        // 这是预期行为
    }
    
    @Test
    void testClose() throws Exception {
        URL[] urls = new URL[0];
        ClassLoader parent = getClass().getClassLoader();
        
        PluginClassLoader classLoader = new PluginClassLoader("test-plugin", urls, parent);
        
        // 关闭不应抛出异常
        assertDoesNotThrow(() -> classLoader.close());
    }
}
