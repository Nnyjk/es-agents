package com.easystation.agent.service;

import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.AgentTemplateVersion;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.dto.AgentTemplateVersionRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentTemplateVersionService {

    @Inject
    ObjectMapper objectMapper;

    /**
     * 获取模板的所有版本
     */
    public List<AgentTemplateVersionRecord> listByTemplate(UUID templateId) {
        return AgentTemplateVersion.<AgentTemplateVersion>find("template.id", Sort.descending("createdAt"), templateId)
                .stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    /**
     * 获取版本详情
     */
    public AgentTemplateVersionRecord get(UUID id) {
        AgentTemplateVersion version = AgentTemplateVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }
        return toRecord(version);
    }

    /**
     * 创建新版本
     */
    @Transactional
    public AgentTemplateVersionRecord create(AgentTemplateVersionRecord.Create dto) {
        AgentTemplate template = AgentTemplate.findById(dto.templateId());
        if (template == null) {
            throw new WebApplicationException("Template not found", Response.Status.NOT_FOUND);
        }

        // 检查版本号是否已存在
        long count = AgentTemplateVersion.count("template.id = ?1 and version = ?2", dto.templateId(), dto.version());
        if (count > 0) {
            throw new WebApplicationException("Version already exists", Response.Status.CONFLICT);
        }

        AgentTemplateVersion version = new AgentTemplateVersion();
        version.template = template;
        version.version = dto.version();
        version.description = dto.description();
        version.installScript = dto.installScript();
        version.configTemplate = dto.configTemplate();
        version.dependencies = dto.dependencies();
        if (dto.osType() != null) {
            try {
                version.osType = OsType.valueOf(dto.osType());
            } catch (IllegalArgumentException e) {
                // ignore invalid osType
            }
        }
        version.archSupport = dto.archSupport();
        version.persist();

        return toRecord(version);
    }

    /**
     * 更新版本
     */
    @Transactional
    public AgentTemplateVersionRecord update(UUID id, AgentTemplateVersionRecord.Update dto) {
        AgentTemplateVersion version = AgentTemplateVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        if (version.published) {
            throw new WebApplicationException("Cannot update published version", Response.Status.BAD_REQUEST);
        }

        if (dto.description() != null) version.description = dto.description();
        if (dto.installScript() != null) version.installScript = dto.installScript();
        if (dto.configTemplate() != null) version.configTemplate = dto.configTemplate();
        if (dto.dependencies() != null) version.dependencies = dto.dependencies();
        if (dto.osType() != null) {
            try {
                version.osType = OsType.valueOf(dto.osType());
            } catch (IllegalArgumentException e) {
                // ignore invalid osType
            }
        }
        if (dto.archSupport() != null) version.archSupport = dto.archSupport();

        return toRecord(version);
    }

    /**
     * 发布版本
     */
    @Transactional
    public AgentTemplateVersionRecord publish(UUID id, AgentTemplateVersionRecord.Publish dto) {
        AgentTemplateVersion version = AgentTemplateVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        if (version.published) {
            throw new WebApplicationException("Version already published", Response.Status.BAD_REQUEST);
        }

        // 将其他版本设为非最新
        AgentTemplateVersion.update("latest = false where template.id = ?1", version.template.id);

        version.published = true;
        version.latest = true;
        version.publishedAt = LocalDateTime.now();
        if (dto != null && dto.description() != null) {
            version.description = dto.description();
        }

        return toRecord(version);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public AgentTemplateVersionRecord rollback(UUID templateId, UUID versionId) {
        AgentTemplateVersion targetVersion = AgentTemplateVersion.findById(versionId);
        if (targetVersion == null || !targetVersion.template.id.equals(templateId)) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        if (!targetVersion.published) {
            throw new WebApplicationException("Cannot rollback to unpublished version", Response.Status.BAD_REQUEST);
        }

        // 将当前最新版本设为非最新
        AgentTemplateVersion.update("latest = false where template.id = ?1 and latest = true", templateId);

        // 设为目标版本为最新
        targetVersion.latest = true;

        return toRecord(targetVersion);
    }

    /**
     * 删除版本（仅未发布的版本可删除）
     */
    @Transactional
    public void delete(UUID id) {
        AgentTemplateVersion version = AgentTemplateVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        if (version.published) {
            throw new WebApplicationException("Cannot delete published version", Response.Status.BAD_REQUEST);
        }

        version.delete();
    }

    /**
     * 版本差异对比
     */
    public String compareVersions(UUID versionId1, UUID versionId2) {
        AgentTemplateVersion v1 = AgentTemplateVersion.findById(versionId1);
        AgentTemplateVersion v2 = AgentTemplateVersion.findById(versionId2);

        if (v1 == null || v2 == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        StringBuilder diff = new StringBuilder();
        diff.append("=== Version Comparison ===\n");
        diff.append("Version 1: ").append(v1.version).append(" (").append(v1.id).append(")\n");
        diff.append("Version 2: ").append(v2.version).append(" (").append(v2.id).append(")\n\n");

        compareField(diff, "Install Script", v1.installScript, v2.installScript);
        compareField(diff, "Config Template", v1.configTemplate, v2.configTemplate);
        compareField(diff, "Dependencies", v1.dependencies, v2.dependencies);
        compareField(diff, "OS Type", 
            v1.osType != null ? v1.osType.name() : "null",
            v2.osType != null ? v2.osType.name() : "null");
        compareField(diff, "Arch Support", v1.archSupport, v2.archSupport);

        return diff.toString();
    }

    private void compareField(StringBuilder diff, String fieldName, String v1, String v2) {
        if ((v1 == null && v2 == null) || (v1 != null && v1.equals(v2))) {
            diff.append(fieldName).append(": [SAME]\n");
        } else {
            diff.append(fieldName).append(": [DIFFERENT]\n");
            diff.append("  - V1: ").append(v1 != null ? v1 : "null").append("\n");
            diff.append("  - V2: ").append(v2 != null ? v2 : "null").append("\n");
        }
    }

    private AgentTemplateVersionRecord toRecord(AgentTemplateVersion v) {
        return new AgentTemplateVersionRecord(
                v.id,
                v.template != null ? v.template.id : null,
                v.template != null ? v.template.name : null,
                v.version,
                v.description,
                v.installScript,
                v.configTemplate,
                v.dependencies,
                v.osType != null ? v.osType.name() : null,
                v.archSupport,
                v.published,
                v.latest,
                v.createdAt,
                v.publishedAt,
                v.createdBy
        );
    }
}