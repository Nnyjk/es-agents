package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.PackageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 打包配置 DTO
 */
public record PackageConfigRecord(
    UUID id,
    String name,
    String description,
    PackageType type,
    // 本地配置
    String localPath,
    // Git 配置
    String gitUrl,
    String gitBranch,
    AgentCredentialRecord.Simple gitCredential,
    // Docker 配置
    String dockerImage,
    String dockerTag,
    AgentCredentialRecord.Simple dockerCredential,
    // 阿里云配置
    String aliyunRepoUrl,
    String aliyunNamespace,
    AgentCredentialRecord.Simple aliyunCredential,
    // 通用配置
    String buildCommand,
    String outputDir,
    Map<String, String> envVariables,
    Integer version,
    Boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {
    /**
     * 创建请求
     */
    public record Create(
        @NotBlank(message = "配置名称不能为空")
        String name,
        String description,
        @NotNull(message = "打包类型不能为空")
        PackageType type,
        // 本地配置
        String localPath,
        // Git 配置
        String gitUrl,
        String gitBranch,
        UUID gitCredentialId,
        // Docker 配置
        String dockerImage,
        String dockerTag,
        UUID dockerCredentialId,
        // 阿里云配置
        String aliyunRepoUrl,
        String aliyunNamespace,
        UUID aliyunCredentialId,
        // 通用配置
        String buildCommand,
        String outputDir,
        Map<String, String> envVariables,
        Boolean enabled
    ) {}

    /**
     * 更新请求
     */
    public record Update(
        String name,
        String description,
        PackageType type,
        // 本地配置
        String localPath,
        // Git 配置
        String gitUrl,
        String gitBranch,
        UUID gitCredentialId,
        // Docker 配置
        String dockerImage,
        String dockerTag,
        UUID dockerCredentialId,
        // 阿里云配置
        String aliyunRepoUrl,
        String aliyunNamespace,
        UUID aliyunCredentialId,
        // 通用配置
        String buildCommand,
        String outputDir,
        Map<String, String> envVariables,
        Boolean enabled
    ) {}
}