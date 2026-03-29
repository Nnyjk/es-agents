package com.easystation.plugin.sandbox.domain;

/**
 * 权限类型枚举
 */
public enum PermissionType {
    FILE_READ,
    FILE_WRITE,
    FILE_DELETE,
    HTTP_CALL,
    DATABASE_ACCESS,
    NETWORK_ACCESS,
    SYSTEM_COMMAND,
    ENV_ACCESS,
    PROCESS_CREATE
}
