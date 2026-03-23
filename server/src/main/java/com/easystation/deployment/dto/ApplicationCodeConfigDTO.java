package com.easystation.deployment.dto;

import lombok.Data;

import java.util.UUID;

/**
 * 应用代码配置 DTO
 * 包含代码仓库、构建脚本等配置信息
 */
@Data
public class ApplicationCodeConfigDTO {
    public String repositoryUrl;
    public String branch;
    public String buildScript;
    public String deployPath;
    public String healthCheckUrl;
    public String buildCommand;
    public String startCommand;
    public String stopCommand;
    public UUID artifactRepositoryId;
}