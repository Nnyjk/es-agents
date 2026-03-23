package com.easystation.plugin.domain.enums;

public enum InstallationStatus {
    INSTALLING("安装中"),
    INSTALLED("已安装"),
    ENABLED("已启用"),
    DISABLED("已禁用"),
    UNINSTALLING("卸载中"),
    UNINSTALLED("已卸载"),
    FAILED("安装失败"),
    UPDATING("更新中");

    private final String description;

    InstallationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}