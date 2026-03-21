package com.easystation.build.enums;

/**
 * 构建类型
 */
public enum BuildType {
    LOCAL_FILE,     // 本地文件上传
    GIT_CLONE,      // Git 仓库克隆
    DOCKER_PULL,    // Docker 镜像拉取
    MAVEN_BUILD,    // Maven 构建
    GRADLE_BUILD,   // Gradle 构建
    SCRIPT_BUILD    // 自定义脚本构建
}