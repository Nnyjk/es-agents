package com.easystation.plugin.sandbox.domain;

import com.easystation.plugin.sandbox.exception.SecurityViolationException;
import com.easystation.plugin.sandbox.exception.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluginSandboxTest {
    
    private PluginSandboxImpl pluginSandbox;
    
    @BeforeEach
    void setUp() {
        pluginSandbox = new PluginSandboxImpl();
    }
    
    @Test
    @DisplayName("测试创建安全上下文")
    void testCreateContext() {
        List<Permission> permissions = List.of(
            Permission.fileRead("/plugins/data/*"),
            Permission.fileWrite("/plugins/data/*")
        );
        
        SecurityContext context = pluginSandbox.createContext("test-plugin", permissions, ResourceLimit.defaults());
        
        assertNotNull(context);
        assertEquals("test-plugin", context.getPluginId());
        assertEquals(2, context.getPermissions().size());
    }
    
    @Test
    @DisplayName("测试权限检查 - 允许")
    void testPermissionCheckAllowed() {
        List<Permission> permissions = List.of(Permission.fileRead("/plugins/data/*"));
        SecurityContext context = pluginSandbox.createContext("test-plugin", permissions, ResourceLimit.defaults());
        
        assertDoesNotThrow(() -> 
            pluginSandbox.checkPermission(context, PermissionType.FILE_READ, "/plugins/data/test.txt")
        );
    }
    
    @Test
    @DisplayName("测试权限检查 - 拒绝")
    void testPermissionCheckDenied() {
        List<Permission> permissions = List.of(Permission.fileRead("/plugins/data/*"));
        SecurityContext context = pluginSandbox.createContext("test-plugin", permissions, ResourceLimit.defaults());
        
        assertThrows(SecurityViolationException.class, () -> 
            pluginSandbox.checkPermission(context, PermissionType.FILE_WRITE, "/plugins/data/test.txt")
        );
    }
    
    @Test
    @DisplayName("测试资源限制 - 文件操作")
    void testResourceLimitFileOperations() {
        ResourceLimit limit = new ResourceLimit(256 * 1024 * 1024, 5, 30, 3, 50);
        SecurityContext context = pluginSandbox.createContext("test-plugin", List.of(), limit);
        
        assertTrue(context.checkFileOperation());
        assertTrue(context.checkFileOperation());
        assertTrue(context.checkFileOperation());
        assertFalse(context.checkFileOperation());
    }
    
    @Test
    @DisplayName("测试执行超时")
    void testExecutionTimeout() throws InterruptedException {
        ResourceLimit limit = new ResourceLimit(256 * 1024 * 1024, 5, 1, 100, 50);
        SecurityContext context = pluginSandbox.createContext("test-plugin", List.of(), limit);
        
        assertTrue(context.checkExecutionTime());
        Thread.sleep(1100);
        assertFalse(context.checkExecutionTime());
    }
    
    @Test
    @DisplayName("测试移除上下文")
    void testRemoveContext() {
        pluginSandbox.createContext("test-plugin", List.of(), ResourceLimit.defaults());
        assertNotNull(pluginSandbox.getContext("test-plugin"));
        pluginSandbox.removeContext("test-plugin");
        assertThrows(IllegalStateException.class, () -> pluginSandbox.getContext("test-plugin"));
    }
}
