package com.easystation.plugin.sandbox.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {
    
    @Test
    @DisplayName("测试权限创建")
    void testPermissionCreation() {
        Permission permission = Permission.fileRead("/plugins/data/*");
        assertEquals(PermissionType.FILE_READ, permission.type());
        assertEquals("/plugins/data/*", permission.scope());
    }
    
    @Test
    @DisplayName("测试权限匹配 - 精确匹配")
    void testPermissionMatchExact() {
        Permission permission = Permission.fileRead("/plugins/data/test.txt");
        assertTrue(permission.matches("/plugins/data/test.txt"));
        assertFalse(permission.matches("/plugins/data/other.txt"));
    }
    
    @Test
    @DisplayName("测试权限匹配 - 通配符")
    void testPermissionMatchWildcard() {
        Permission permission = Permission.fileRead("/plugins/data/*");
        assertTrue(permission.matches("/plugins/data/test.txt"));
        assertTrue(permission.matches("/plugins/data/subdir/file.txt"));
        assertFalse(permission.matches("/plugins/other/test.txt"));
    }
    
    @Test
    @DisplayName("测试 HTTP 权限")
    void testHttpPermission() {
        Permission permission = Permission.httpCall("https://api.example.com/*");
        assertEquals(PermissionType.HTTP_CALL, permission.type());
        assertTrue(permission.matches("https://api.example.com/users"));
        assertFalse(permission.matches("https://other.com/api"));
    }
}
