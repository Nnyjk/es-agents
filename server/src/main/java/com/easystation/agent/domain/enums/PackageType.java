package com.easystation.agent.domain.enums;

/**
 * 打包类型枚举
 * 支持多种打包来源方式
 */
public enum PackageType {
    /** 本地上传 */
    LOCAL,
    /** Git 仓库 */
    GIT,
    /** Docker 镜像仓库 */
    DOCKER,
    /** 阿里云仓库 */
    ALIYUN
}