package com.easystation.audit.enums;

/**
 * 审计操作类型
 */
public enum AuditAction {
    // 用户认证
    LOGIN("用户登录"),
    LOGOUT("用户登出"),
    LOGIN_FAILED("登录失败"),

    // 资源创建
    CREATE_HOST("创建主机"),
    CREATE_ENVIRONMENT("创建环境"),
    CREATE_TEMPLATE("创建模板"),
    CREATE_INSTANCE("创建实例"),
    CREATE_COMMAND("创建命令"),

    // 资源更新
    UPDATE_HOST("更新主机"),
    UPDATE_ENVIRONMENT("更新环境"),
    UPDATE_TEMPLATE("更新模板"),
    UPDATE_INSTANCE("更新实例"),
    UPDATE_COMMAND("更新命令"),

    // 资源删除
    DELETE_HOST("删除主机"),
    DELETE_ENVIRONMENT("删除环境"),
    DELETE_TEMPLATE("删除模板"),
    DELETE_INSTANCE("删除实例"),
    DELETE_COMMAND("删除命令"),

    // 操作执行
    EXECUTE_COMMAND("执行命令"),
    DEPLOY_AGENT("部署Agent"),
    START_AGENT("启动Agent"),
    STOP_AGENT("停止Agent"),
    RESTART_AGENT("重启Agent"),

    // 系统配置
    UPDATE_CONFIG("更新配置"),
    CREATE_USER("创建用户"),
    UPDATE_USER("更新用户"),
    DELETE_USER("删除用户"),
    ASSIGN_ROLE("分配角色"),

    // 其他
    EXPORT_DATA("导出数据"),
    IMPORT_DATA("导入数据"),
    CUSTOM("自定义操作");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}