package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.AgentSourceType;
import com.easystation.agent.domain.enums.CommandCategory;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.domain.enums.TemplateCategory;
import com.easystation.agent.dto.AgentCommandRecord;
import com.easystation.agent.dto.AgentSourceRecord;
import com.easystation.agent.dto.AgentTemplateRecord;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentTemplateService {

    @Inject
    AgentSourceService agentSourceService;

    /**
     * 列表查询（支持分类筛选）
     */
    public List<AgentTemplateRecord> list(String osType, String sourceType, String category) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (osType != null) {
            query.append(" AND osType = :osType");
            params.put("osType", OsType.valueOf(osType.toUpperCase()));
        }
        if (sourceType != null) {
            query.append(" AND source.type = :sourceType");
            params.put("sourceType", AgentSourceType.valueOf(sourceType.toUpperCase()));
        }
        if (category != null) {
            query.append(" AND category = :category");
            params.put("category", TemplateCategory.valueOf(category.toUpperCase()));
        }

        return AgentTemplate.find(query.toString(), Sort.ascending("name"), params).stream()
                .map(e -> (AgentTemplate) e)
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    /**
     * 获取详情
     */
    public AgentTemplateRecord get(UUID id) {
        AgentTemplate template = AgentTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        return toRecord(template);
    }

    /**
     * 创建模板
     */
    @Transactional
    public AgentTemplateRecord create(AgentTemplateRecord.Create dto) {
        // 验证名称唯一性
        if (AgentTemplate.find("name", dto.name()).firstResult() != null) {
            throw new WebApplicationException("Agent Template with name '" + dto.name() + "' already exists", Response.Status.CONFLICT);
        }

        AgentSource source = null;
        if (dto.sourceId() != null) {
            source = AgentSource.findById(dto.sourceId());
            if (source == null) {
                throw new WebApplicationException("Agent Source not found", Response.Status.BAD_REQUEST);
            }
        }

        AgentTemplate template = new AgentTemplate();
        template.name = dto.name();
        template.description = dto.description();
        if (dto.category() != null) {
            try {
                template.category = TemplateCategory.valueOf(dto.category().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        if (dto.osType() != null) {
            try {
                template.osType = OsType.valueOf(dto.osType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        template.archSupport = dto.archSupport();
        template.installScript = dto.installScript();
        template.configTemplate = dto.configTemplate();
        template.dependencies = dto.dependencies();
        template.source = source;
        template.deploymentCount = 0;
        template.successCount = 0;

        if (dto.commands() != null && !dto.commands().isEmpty()) {
            template.commands = new java.util.ArrayList<>();
            for (AgentCommandRecord.Create cmdDto : dto.commands()) {
                AgentCommand cmd = new AgentCommand();
                cmd.name = cmdDto.name();
                cmd.command = cmdDto.script();
                cmd.description = cmdDto.description();
                if (cmdDto.category() != null) {
                    try {
                        cmd.category = CommandCategory.valueOf(cmdDto.category().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
                cmd.order = cmdDto.order() != null ? cmdDto.order() : 0;
                cmd.template = template;
                template.commands.add(cmd);
            }
        }

        template.persist();
        return toRecord(template);
    }

    /**
     * 更新模板
     */
    @Transactional
    public AgentTemplateRecord update(UUID id, AgentTemplateRecord.Update dto) {
        AgentTemplate template = AgentTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }

        // 验证名称唯一性
        if (dto.name() != null && !dto.name().equals(template.name)) {
            AgentTemplate existing = AgentTemplate.find("name", dto.name()).firstResult();
            if (existing != null && !existing.id.equals(id)) {
                throw new WebApplicationException("Agent Template with name '" + dto.name() + "' already exists", Response.Status.CONFLICT);
            }
            template.name = dto.name();
        }

        if (dto.description() != null) template.description = dto.description();
        if (dto.category() != null) {
            try {
                template.category = TemplateCategory.valueOf(dto.category().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        if (dto.osType() != null) {
            try {
                template.osType = OsType.valueOf(dto.osType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        if (dto.archSupport() != null) template.archSupport = dto.archSupport();
        if (dto.installScript() != null) template.installScript = dto.installScript();
        if (dto.configTemplate() != null) template.configTemplate = dto.configTemplate();
        if (dto.dependencies() != null) template.dependencies = dto.dependencies();

        if (dto.sourceId() != null) {
            AgentSource source = AgentSource.findById(dto.sourceId());
            if (source == null) {
                throw new WebApplicationException("Agent Source not found", Response.Status.BAD_REQUEST);
            }
            template.source = source;
        }

        return toRecord(template);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void delete(UUID id) {
        if (!AgentTemplate.deleteById(id)) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * 获取模板使用统计
     */
    public AgentTemplateRecord.Statistics getStatistics(UUID templateId) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }

        // 获取关联实例数
        long instanceCount = AgentInstance.count("template.id", templateId);

        int deploymentCount = template.deploymentCount != null ? template.deploymentCount : 0;
        int successCount = template.successCount != null ? template.successCount : 0;
        int failureCount = deploymentCount - successCount;
        double successRate = deploymentCount > 0 ? (double) successCount / deploymentCount * 100 : 0.0;

        return new AgentTemplateRecord.Statistics(
                template.id,
                template.name,
                deploymentCount,
                successCount,
                failureCount,
                successRate,
                (int) instanceCount
        );
    }

    /**
     * 更新部署统计
     */
    @Transactional
    public void updateDeploymentStats(UUID templateId, boolean success) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template != null) {
            template.deploymentCount = (template.deploymentCount != null ? template.deploymentCount : 0) + 1;
            if (success) {
                template.successCount = (template.successCount != null ? template.successCount : 0) + 1;
            }
        }
    }

    /**
     * 获取所有分类
     */
    public List<Map<String, String>> listCategories() {
        return java.util.Arrays.stream(TemplateCategory.values())
                .map(c -> Map.of(
                        "name", c.name(),
                        "description", c.getDescription()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 下载Agent包
     */
    public InputStream download(UUID templateId, String[] fileNameOut) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        if (template.source == null) {
            throw new WebApplicationException("Template has no source configured", Response.Status.BAD_REQUEST);
        }
        return agentSourceService.getSourceStream(template.source.id, fileNameOut);
    }

    private AgentTemplateRecord toRecord(AgentTemplate t) {
        List<AgentCommandRecord> commands = t.commands != null ?
                t.commands.stream()
                        .sorted(Comparator.comparingInt(c -> c.order != null ? c.order : 0))
                        .map(cmd -> new AgentCommandRecord(
                                cmd.id,
                                cmd.name,
                                cmd.command,
                                cmd.description,
                                cmd.category != null ? cmd.category.name() : null,
                                cmd.order,
                                t.id,
                                t.name
                        ))
                        .collect(Collectors.toList()) :
                List.of();

        Double successRate = t.deploymentCount != null && t.deploymentCount > 0 ?
                (t.successCount != null ? (double) t.successCount / t.deploymentCount * 100 : 0.0) : null;

        return new AgentTemplateRecord(
                t.id,
                t.name,
                t.description,
                t.category != null ? t.category.name() : null,
                t.osType != null ? t.osType.name() : null,
                t.archSupport,
                t.installScript,
                t.configTemplate,
                t.dependencies,
                t.source != null ? toSourceRecord(t.source) : null,
                commands,
                t.deploymentCount,
                t.successCount,
                successRate,
                t.createdAt,
                t.updatedAt
        );
    }

    private AgentSourceRecord toSourceRecord(AgentSource source) {
        return new AgentSourceRecord(
                source.id,
                source.name,
                source.type != null ? source.type.name() : null,
                source.url,
                source.description,
                source.createdAt
        );
    }
}