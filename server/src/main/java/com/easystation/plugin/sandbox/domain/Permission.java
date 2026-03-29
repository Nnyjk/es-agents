package com.easystation.plugin.sandbox.domain;

/**
 * 权限定义，支持通配符匹配
 */
public record Permission(
    PermissionType type,
    String scope,
    String description
) {
    
    public static Permission fileRead(String pathPattern) {
        return new Permission(PermissionType.FILE_READ, pathPattern, "File read access");
    }
    
    public static Permission fileWrite(String pathPattern) {
        return new Permission(PermissionType.FILE_WRITE, pathPattern, "File write access");
    }
    
    public static Permission fileDelete(String pathPattern) {
        return new Permission(PermissionType.FILE_DELETE, pathPattern, "File delete access");
    }
    
    public static Permission httpCall(String urlPattern) {
        return new Permission(PermissionType.HTTP_CALL, urlPattern, "HTTP call access");
    }
    
    public static Permission databaseAccess(String jdbcUrl) {
        return new Permission(PermissionType.DATABASE_ACCESS, jdbcUrl, "Database access");
    }
    
    public static Permission networkAccess(String hostPattern) {
        return new Permission(PermissionType.NETWORK_ACCESS, hostPattern, "Network access");
    }
    
    public static Permission systemCommand(String... commands) {
        return new Permission(PermissionType.SYSTEM_COMMAND, String.join(",", commands), "System command execution");
    }
    
    public static Permission envAccess(String... envNames) {
        return new Permission(PermissionType.ENV_ACCESS, String.join(",", envNames), "Environment variable access");
    }
    
    public static Permission processCreate(String commandPattern) {
        return new Permission(PermissionType.PROCESS_CREATE, commandPattern, "Process creation");
    }
    
    /**
     * 检查路径/URL 是否匹配权限范围
     */
    public boolean matches(String target) {
        if (scope.equals("*")) {
            return true;
        }
        if (scope.endsWith("*")) {
            String prefix = scope.substring(0, scope.length() - 1);
            return target.startsWith(prefix);
        }
        if (scope.endsWith("**")) {
            String prefix = scope.substring(0, scope.length() - 2);
            return target.startsWith(prefix);
        }
        return scope.equals(target);
    }
}
