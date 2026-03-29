package com.easystation.plugin.sandbox.service;

import com.easystation.plugin.sandbox.domain.Permission;
import com.easystation.plugin.sandbox.exception.SecurityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCheckerTest {
    
    private SecurityChecker securityChecker;
    
    @BeforeEach
    void setUp() {
        securityChecker = new SecurityChecker();
    }
    
    @Test
    @DisplayName("测试文件读取权限检查 - 允许")
    void testFileReadAllowed() {
        List<Permission> permissions = List.of(Permission.fileRead("/plugins/data/*"));
        assertDoesNotThrow(() -> securityChecker.checkFileRead(permissions, "/plugins/data/test.txt"));
    }
    
    @Test
    @DisplayName("测试文件读取权限检查 - 拒绝")
    void testFileReadDenied() {
        List<Permission> permissions = List.of(Permission.fileRead("/plugins/data/*"));
        assertThrows(SecurityViolationException.class, () -> 
            securityChecker.checkFileRead(permissions, "/etc/passwd"));
    }
    
    @Test
    @DisplayName("测试文件写入权限检查 - 允许")
    void testFileWriteAllowed() {
        List<Permission> permissions = List.of(Permission.fileWrite("/plugins/data/*"));
        assertDoesNotThrow(() -> securityChecker.checkFileWrite(permissions, "/plugins/data/output.txt"));
    }
    
    @Test
    @DisplayName("测试 HTTP 调用权限检查 - 允许")
    void testHttpCallAllowed() {
        List<Permission> permissions = List.of(Permission.httpCall("https://api.example.com/*"));
        assertDoesNotThrow(() -> securityChecker.checkHttpCall(permissions, "https://api.example.com/users"));
    }
    
    @Test
    @DisplayName("测试 HTTP 调用权限检查 - 拒绝")
    void testHttpCallDenied() {
        List<Permission> permissions = List.of(Permission.httpCall("https://api.example.com/*"));
        assertThrows(SecurityViolationException.class, () -> 
            securityChecker.checkHttpCall(permissions, "https://malicious.com/attack"));
    }
}
