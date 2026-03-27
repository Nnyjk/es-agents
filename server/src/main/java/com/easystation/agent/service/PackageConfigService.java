package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCredential;
import com.easystation.agent.domain.PackageConfig;
import com.easystation.agent.domain.enums.PackageType;
import com.easystation.agent.dto.PackageConfigRecord;
import com.easystation.agent.mapper.PackageConfigMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 打包配置服务
 */
@ApplicationScoped
public class PackageConfigService {

    @Inject
    PackageConfigMapper mapper;

    /**
     * 获取所有打包配置
     */
    public List<PackageConfigRecord> list() {
        return PackageConfig.listAll().stream()
            .map(e -> (PackageConfig) e)
            .map(mapper::toRecord)
            .collect(Collectors.toList());
    }

    /**
     * 获取启用的打包配置
     */
    public List<PackageConfigRecord> listEnabled() {
        return PackageConfig.find("enabled", true).list().stream()
            .map(e -> (PackageConfig) e)
            .map(mapper::toRecord)
            .collect(Collectors.toList());
    }

    /**
     * 根据类型获取打包配置
     */
    public List<PackageConfigRecord> listByType(PackageType type) {
        return PackageConfig.find("type", type).list().stream()
            .map(e -> (PackageConfig) e)
            .map(mapper::toRecord)
            .collect(Collectors.toList());
    }

    /**
     * 获取单个打包配置
     */
    public PackageConfigRecord get(UUID id) {
        PackageConfig config = PackageConfig.findById(id);
        if (config == null) {
            throw new WebApplicationException("Package Config not found", Response.Status.NOT_FOUND);
        }
        return mapper.toRecord(config);
    }

    /**
     * 创建打包配置
     */
    @Transactional
    public PackageConfigRecord create(PackageConfigRecord.Create dto) {
        // 检查名称是否重复
        if (PackageConfig.find("name", dto.name()).firstResult() != null) {
            throw new WebApplicationException("Package Config name already exists: " + dto.name(), Response.Status.CONFLICT);
        }

        PackageConfig config = new PackageConfig();
        mapper.createEntity(dto, config);

        // 设置凭证关联
        config.gitCredential = resolveCredential(dto.gitCredentialId());
        config.dockerCredential = resolveCredential(dto.dockerCredentialId());
        config.aliyunCredential = resolveCredential(dto.aliyunCredentialId());

        config.persist();
        return mapper.toRecord(config);
    }

    /**
     * 更新打包配置
     */
    @Transactional
    public PackageConfigRecord update(UUID id, PackageConfigRecord.Update dto) {
        PackageConfig config = PackageConfig.findById(id);
        if (config == null) {
            throw new WebApplicationException("Package Config not found", Response.Status.NOT_FOUND);
        }

        // 检查名称是否重复（排除自身）
        if (dto.name() != null && !dto.name().equals(config.name)) {
            if (PackageConfig.find("name", dto.name()).firstResult() != null) {
                throw new WebApplicationException("Package Config name already exists: " + dto.name(), Response.Status.CONFLICT);
            }
        }

        mapper.updateEntity(dto, config);

        // 更新凭证关联
        if (dto.gitCredentialId() != null) {
            config.gitCredential = resolveCredential(dto.gitCredentialId());
        }
        if (dto.dockerCredentialId() != null) {
            config.dockerCredential = resolveCredential(dto.dockerCredentialId());
        }
        if (dto.aliyunCredentialId() != null) {
            config.aliyunCredential = resolveCredential(dto.aliyunCredentialId());
        }

        return mapper.toRecord(config);
    }

    /**
     * 删除打包配置（软删除）
     */
    @Transactional
    public void delete(UUID id) {
        PackageConfig config = PackageConfig.findById(id);
        if (config == null) {
            throw new WebApplicationException("Package Config not found", Response.Status.NOT_FOUND);
        }
        config.deleted = true;
        config.persist();
    }

    /**
     * 启用/禁用打包配置
     */
    @Transactional
    public PackageConfigRecord setEnabled(UUID id, boolean enabled) {
        PackageConfig config = PackageConfig.findById(id);
        if (config == null) {
            throw new WebApplicationException("Package Config not found", Response.Status.NOT_FOUND);
        }
        config.enabled = enabled;
        return mapper.toRecord(config);
    }

    /**
     * 复制打包配置
     */
    @Transactional
    public PackageConfigRecord duplicate(UUID id) {
        PackageConfig source = PackageConfig.findById(id);
        if (source == null) {
            throw new WebApplicationException("Package Config not found", Response.Status.NOT_FOUND);
        }

        PackageConfig copy = new PackageConfig();
        copy.name = generateCopyName(source.name);
        copy.description = source.description;
        copy.type = source.type;
        copy.localPath = source.localPath;
        copy.gitUrl = source.gitUrl;
        copy.gitBranch = source.gitBranch;
        copy.gitCredential = source.gitCredential;
        copy.dockerImage = source.dockerImage;
        copy.dockerTag = source.dockerTag;
        copy.dockerCredential = source.dockerCredential;
        copy.aliyunRepoUrl = source.aliyunRepoUrl;
        copy.aliyunNamespace = source.aliyunNamespace;
        copy.aliyunCredential = source.aliyunCredential;
        copy.buildCommand = source.buildCommand;
        copy.outputDir = source.outputDir;
        copy.envVariables = source.envVariables;
        copy.enabled = false; // 复制的配置默认禁用
        copy.persist();

        return mapper.toRecord(copy);
    }

    /**
     * 解析凭证
     */
    private AgentCredential resolveCredential(UUID id) {
        if (id == null) {
            return null;
        }
        AgentCredential credential = AgentCredential.findById(id);
        if (credential == null) {
            throw new WebApplicationException("Credential not found: " + id, Response.Status.BAD_REQUEST);
        }
        return credential;
    }

    /**
     * 生成复制名称
     */
    private String generateCopyName(String originalName) {
        String baseName = originalName;
        int suffix = 1;

        // 如果原名称已包含 "-copy" 后缀，提取基础名称
        if (originalName.matches(".*-copy(-\\d+)?$")) {
            int lastCopyIndex = originalName.lastIndexOf("-copy");
            baseName = originalName.substring(0, lastCopyIndex);
        }

        // 查找可用的名称
        String newName;
        do {
            newName = baseName + "-copy-" + suffix;
            suffix++;
        } while (PackageConfig.find("name", newName).firstResult() != null);

        return newName;
    }
}