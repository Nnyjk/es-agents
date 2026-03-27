package com.easystation.agent.mapper;

import com.easystation.agent.domain.PackageConfig;
import com.easystation.agent.dto.AgentCredentialRecord;
import com.easystation.agent.dto.PackageConfigRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Map;

/**
 * 打包配置 Mapper
 */
@ApplicationScoped
public class PackageConfigMapper {

    @Inject
    ObjectMapper objectMapper;

    public PackageConfigRecord toRecord(PackageConfig entity) {
        if (entity == null) {
            return null;
        }

        return new PackageConfigRecord(
            entity.id,
            entity.name,
            entity.description,
            entity.type,
            // 本地配置
            entity.localPath,
            // Git 配置
            entity.gitUrl,
            entity.gitBranch,
            toSimpleCredential(entity.gitCredential),
            // Docker 配置
            entity.dockerImage,
            entity.dockerTag,
            toSimpleCredential(entity.dockerCredential),
            // 阿里云配置
            entity.aliyunRepoUrl,
            entity.aliyunNamespace,
            toSimpleCredential(entity.aliyunCredential),
            // 通用配置
            entity.buildCommand,
            entity.outputDir,
            parseEnvVariables(entity.envVariables),
            entity.version,
            entity.enabled,
            entity.createdAt,
            entity.updatedAt,
            entity.createdBy,
            entity.updatedBy
        );
    }

    public void createEntity(PackageConfigRecord.Create dto, PackageConfig entity) {
        if (entity == null || dto == null) {
            return;
        }
        entity.name = dto.name();
        entity.description = dto.description();
        entity.type = dto.type();
        // 本地配置
        entity.localPath = dto.localPath();
        // Git 配置
        entity.gitUrl = dto.gitUrl();
        entity.gitBranch = dto.gitBranch();
        // Docker 配置
        entity.dockerImage = dto.dockerImage();
        entity.dockerTag = dto.dockerTag();
        // 阿里云配置
        entity.aliyunRepoUrl = dto.aliyunRepoUrl();
        entity.aliyunNamespace = dto.aliyunNamespace();
        // 通用配置
        entity.buildCommand = dto.buildCommand();
        entity.outputDir = dto.outputDir();
        entity.envVariables = serializeEnvVariables(dto.envVariables());
        if (dto.enabled() != null) {
            entity.enabled = dto.enabled();
        }
    }

    public void updateEntity(PackageConfigRecord.Update dto, PackageConfig entity) {
        if (entity == null || dto == null) {
            return;
        }
        // 更新时版本号递增
        entity.version = entity.version + 1;

        if (dto.name() != null) {
            entity.name = dto.name();
        }
        if (dto.description() != null) {
            entity.description = dto.description();
        }
        if (dto.type() != null) {
            entity.type = dto.type();
        }
        // 本地配置
        if (dto.localPath() != null) {
            entity.localPath = dto.localPath();
        }
        // Git 配置
        if (dto.gitUrl() != null) {
            entity.gitUrl = dto.gitUrl();
        }
        if (dto.gitBranch() != null) {
            entity.gitBranch = dto.gitBranch();
        }
        // Docker 配置
        if (dto.dockerImage() != null) {
            entity.dockerImage = dto.dockerImage();
        }
        if (dto.dockerTag() != null) {
            entity.dockerTag = dto.dockerTag();
        }
        // 阿里云配置
        if (dto.aliyunRepoUrl() != null) {
            entity.aliyunRepoUrl = dto.aliyunRepoUrl();
        }
        if (dto.aliyunNamespace() != null) {
            entity.aliyunNamespace = dto.aliyunNamespace();
        }
        // 通用配置
        if (dto.buildCommand() != null) {
            entity.buildCommand = dto.buildCommand();
        }
        if (dto.outputDir() != null) {
            entity.outputDir = dto.outputDir();
        }
        if (dto.envVariables() != null) {
            entity.envVariables = serializeEnvVariables(dto.envVariables());
        }
        if (dto.enabled() != null) {
            entity.enabled = dto.enabled();
        }
    }

    private AgentCredentialRecord.Simple toSimpleCredential(com.easystation.agent.domain.AgentCredential credential) {
        if (credential == null) {
            return null;
        }
        return new AgentCredentialRecord.Simple(
            credential.id,
            credential.name,
            credential.type
        );
    }

    private Map<String, String> parseEnvVariables(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String serializeEnvVariables(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}