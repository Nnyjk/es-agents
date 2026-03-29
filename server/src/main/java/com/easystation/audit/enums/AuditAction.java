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
    CREATE_APPLICATION("创建应用"),
    CREATE_APPLICATION_CONFIG("创建应用配置"),
    CREATE_APPLICATION_DEPENDENCY("创建应用依赖"),
    CREATE_ARTIFACT_REPOSITORY("创建制品仓库"),
    CREATE_DEPLOYMENT_CHANGE("创建部署变更"),
    CREATE_DEPLOYMENT_VERSION("创建部署版本"),
    CREATE_DEPLOY_STRATEGY("创建部署策略"),
    CREATE_PIPELINE("创建流水线"),

    // 资源更新
    UPDATE_HOST("更新主机"),
    UPDATE_ENVIRONMENT("更新环境"),
    UPDATE_TEMPLATE("更新模板"),
    UPDATE_INSTANCE("更新实例"),
    UPDATE_COMMAND("更新命令"),
    UPDATE_APPLICATION("更新应用"),
    UPDATE_APPLICATION_CONFIG("更新应用配置"),
    UPDATE_APPLICATION_DEPENDENCY("更新应用依赖"),
    UPDATE_ARTIFACT_REPOSITORY("更新制品仓库"),
    UPDATE_DEPLOYMENT_VERSION("更新部署版本"),
    UPDATE_DEPLOY_STRATEGY("更新部署策略"),
    UPDATE_PIPELINE("更新流水线"),

    // 资源删除
    DELETE_HOST("删除主机"),
    DELETE_ENVIRONMENT("删除环境"),
    DELETE_TEMPLATE("删除模板"),
    DELETE_INSTANCE("删除实例"),
    DELETE_COMMAND("删除命令"),
    DELETE_APPLICATION("删除应用"),
    DELETE_APPLICATION_CONFIG("删除应用配置"),
    DELETE_APPLICATION_DEPENDENCY("删除应用依赖"),
    DELETE_ARTIFACT_REPOSITORY("删除制品仓库"),
    DELETE_DEPLOYMENT_VERSION("删除部署版本"),
    DELETE_DEPLOY_STRATEGY("删除部署策略"),
    DELETE_PIPELINE("删除流水线"),

    // 操作执行
    EXECUTE_COMMAND("执行命令"),
    DEPLOY_AGENT("部署Agent"),
    START_AGENT("启动Agent"),
    STOP_AGENT("停止Agent"),
    RESTART_AGENT("重启Agent"),
    TRIGGER_PIPELINE("触发流水线"),
    CANCEL_PIPELINE("取消流水线"),
    RETRY_PIPELINE("重试流水线"),
    ARCHIVE_APPLICATION("归档应用"),
    ACTIVATE_RESOURCE("激活资源"),
    DEACTIVATE_RESOURCE("停用资源"),
    SET_DEFAULT_RESOURCE("设置默认资源"),
    MARK_STAGE_COMPLETE("标记阶段完成"),
    MARK_STAGE_FAILED("标记阶段失败"),

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