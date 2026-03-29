package com.easystation.plugin.sandbox.service;

import com.easystation.plugin.sandbox.domain.Permission;
import com.easystation.plugin.sandbox.domain.PermissionType;
import com.easystation.plugin.sandbox.exception.SecurityViolationException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * 安全检查器服务
 */
@ApplicationScoped
public class SecurityChecker {
    
    private static final Logger LOG = Logger.getLogger(SecurityChecker.class);
    
    /**
     * 检查文件读取权限
     */
    public void checkFileRead(List<Permission> permissions, String path) {
        if (!hasPermission(permissions, PermissionType.FILE_READ, path)) {
            throw new SecurityViolationException(
                String.format("No permission to read file: %s", path)
            );
        }
        LOG.debugf("File read allowed: %s", path);
    }
    
    /**
     * 检查文件写入权限
     */
    public void checkFileWrite(List<Permission> permissions, String path) {
        if (!hasPermission(permissions, PermissionType.FILE_WRITE, path)) {
            throw new SecurityViolationException(
                String.format("No permission to write file: %s", path)
            );
        }
        LOG.debugf("File write allowed: %s", path);
    }
    
    /**
     * 检查 HTTP 调用权限
     */
    public void checkHttpCall(List<Permission> permissions, String url) {
        if (!hasPermission(permissions, PermissionType.HTTP_CALL, url)) {
            throw new SecurityViolationException(
                String.format("No permission to call URL: %s", url)
            );
        }
        LOG.debugf("HTTP call allowed: %s", url);
    }
    
    /**
     * 检查数据库访问权限
     */
    public void checkDatabaseAccess(List<Permission> permissions, String jdbcUrl) {
        if (!hasPermission(permissions, PermissionType.DATABASE_ACCESS, jdbcUrl)) {
            throw new SecurityViolationException(
                String.format("No permission to access database: %s", jdbcUrl)
            );
        }
        LOG.debugf("Database access allowed: %s", jdbcUrl);
    }
    
    /**
     * 检查网络访问权限
     */
    public void checkNetworkAccess(List<Permission> permissions, String host) {
        if (!hasPermission(permissions, PermissionType.NETWORK_ACCESS, host)) {
            throw new SecurityViolationException(
                String.format("No permission to access host: %s", host)
            );
        }
        LOG.debugf("Network access allowed: %s", host);
    }
    
    /**
     * 检查系统命令执行权限
     */
    public void checkSystemCommand(List<Permission> permissions, String command) {
        if (!hasPermission(permissions, PermissionType.SYSTEM_COMMAND, command)) {
            throw new SecurityViolationException(
                String.format("No permission to execute command: %s", command)
            );
        }
        LOG.debugf("System command allowed: %s", command);
    }
    
    private boolean hasPermission(List<Permission> permissions, PermissionType type, String target) {
        return permissions.stream()
            .filter(p -> p.type() == type)
            .anyMatch(p -> p.matches(target));
    }
}
