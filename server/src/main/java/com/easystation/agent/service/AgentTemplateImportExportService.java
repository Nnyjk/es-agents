package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.domain.enums.TemplateCategory;
import com.easystation.agent.dto.AgentCommandRecord;
import com.easystation.agent.dto.AgentTemplateRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentTemplateImportExportService {

    @Inject
    ObjectMapper objectMapper;

    /**
     * 导出模板为JSON格式
     */
    public String exportAsJson(UUID templateId) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Template not found", Response.Status.NOT_FOUND);
        }
        return exportTemplate(template, false);
    }

    /**
     * 导出模板为YAML格式
     */
    public String exportAsYaml(UUID templateId) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Template not found", Response.Status.NOT_FOUND);
        }
        return exportTemplate(template, true);
    }

    /**
     * 批量导出模板
     */
    public String exportBatch(List<UUID> templateIds, boolean asYaml) {
        List<AgentTemplate> templates = AgentTemplate.list("id in ?1", templateIds);
        if (templates.isEmpty()) {
            throw new WebApplicationException("No templates found", Response.Status.NOT_FOUND);
        }

        List<Map<String, Object>> exportData = templates.stream()
                .map(this::toExportMap)
                .collect(Collectors.toList());

        try {
            if (asYaml) {
                ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                return yamlMapper.writeValueAsString(Map.of("templates", exportData));
            } else {
                return objectMapper.writeValueAsString(Map.of("templates", exportData));
            }
        } catch (IOException e) {
            throw new WebApplicationException("Export failed: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从文件导入模板
     */
    @Transactional
    public AgentTemplateRecord importTemplate(String content, UUID sourceId, String format) {
        // 验证模板格式
        validateTemplateFormat(content, format);

        try {
            Map<String, Object> data;
            if ("yaml".equalsIgnoreCase(format)) {
                ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                data = yamlMapper.readValue(content, Map.class);
            } else {
                data = objectMapper.readValue(content, Map.class);
            }

            AgentTemplateRecord.ImportData importData = parseImportData(data);
            return createTemplateFromImport(importData, sourceId);
        } catch (IOException e) {
            throw new WebApplicationException("Import failed: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 从URL导入模板
     */
    @Transactional
    public AgentTemplateRecord importFromUrl(String url, UUID sourceId) {
        try {
            // 使用Java标准库获取URL内容
            java.net.URL templateUrl = new java.net.URL(url);
            String content;
            try (InputStream is = templateUrl.openStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                content = baos.toString(StandardCharsets.UTF_8);
            }

            // 检测格式
            String format = content.trim().startsWith("{") ? "json" : "yaml";
            return importTemplate(content, sourceId, format);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to fetch template from URL: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 校验模板格式
     */
    public Map<String, Object> validateTemplateFormat(String content, String format) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        List<String> errors = new ArrayList<>();

        try {
            Map<String, Object> data;
            if ("yaml".equalsIgnoreCase(format)) {
                ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                data = yamlMapper.readValue(content, Map.class);
            } else {
                data = objectMapper.readValue(content, Map.class);
            }

            // 校验必填字段
            if (data.get("name") == null || ((String) data.get("name")).isBlank()) {
                errors.add("name is required");
            }

            // 校验osType
            if (data.get("osType") != null) {
                try {
                    OsType.valueOf((String) data.get("osType"));
                } catch (IllegalArgumentException e) {
                    errors.add("invalid osType: " + data.get("osType"));
                }
            }

            // 校验category
            if (data.get("category") != null) {
                try {
                    TemplateCategory.valueOf((String) data.get("category"));
                } catch (IllegalArgumentException e) {
                    errors.add("invalid category: " + data.get("category"));
                }
            }

        } catch (IOException e) {
            result.put("valid", false);
            errors.add("Invalid " + format.toUpperCase() + " format: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            result.put("valid", false);
            result.put("errors", errors);
        }

        return result;
    }

    private String exportTemplate(AgentTemplate template, boolean asYaml) {
        Map<String, Object> exportData = toExportMap(template);
        try {
            if (asYaml) {
                ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                return yamlMapper.writeValueAsString(exportData);
            } else {
                return objectMapper.writeValueAsString(exportData);
            }
        } catch (IOException e) {
            throw new WebApplicationException("Export failed: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> toExportMap(AgentTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", template.name);
        map.put("description", template.description);
        map.put("category", template.category != null ? template.category.name() : null);
        map.put("osType", template.osType != null ? template.osType.name() : null);
        map.put("archSupport", template.archSupport);
        map.put("installScript", template.installScript);
        map.put("configTemplate", template.configTemplate);
        map.put("dependencies", template.dependencies);

        // 导出命令
        if (template.commands != null && !template.commands.isEmpty()) {
            List<Map<String, Object>> commands = template.commands.stream()
                    .map(cmd -> {
                        Map<String, Object> cmdMap = new HashMap<>();
                        cmdMap.put("name", cmd.name);
                        cmdMap.put("command", cmd.command);
                        cmdMap.put("description", cmd.description);
                        cmdMap.put("category", cmd.category != null ? cmd.category.name() : null);
                        cmdMap.put("order", cmd.order);
                        return cmdMap;
                    })
                    .collect(Collectors.toList());
            map.put("commands", commands);
        }

        // 添加导出元信息
        map.put("exportVersion", "1.0");
        map.put("exportedAt", java.time.LocalDateTime.now().toString());

        return map;
    }

    private AgentTemplateRecord.ImportData parseImportData(Map<String, Object> data) {
        List<AgentCommandRecord.Create> commands = null;
        if (data.get("commands") != null) {
            List<Map<String, Object>> cmdList = (List<Map<String, Object>>) data.get("commands");
            commands = cmdList.stream()
                    .map(cmd -> new AgentCommandRecord.Create(
                            (String) cmd.get("name"),
                            (String) cmd.get("command"),
                            (String) cmd.get("description"),
                            (String) cmd.get("category"),
                            cmd.get("order") != null ? ((Number) cmd.get("order")).intValue() : null
                    ))
                    .collect(Collectors.toList());
        }

        return new AgentTemplateRecord.ImportData(
                (String) data.get("name"),
                (String) data.get("description"),
                (String) data.get("category"),
                (String) data.get("osType"),
                (String) data.get("archSupport"),
                (String) data.get("installScript"),
                (String) data.get("configTemplate"),
                (String) data.get("dependencies"),
                commands
        );
    }

    @Transactional
    AgentTemplateRecord createTemplateFromImport(AgentTemplateRecord.ImportData importData, UUID sourceId) {
        AgentSource source = null;
        if (sourceId != null) {
            source = AgentSource.findById(sourceId);
        }

        AgentTemplate template = new AgentTemplate();
        template.name = importData.name();
        template.description = importData.description();
        if (importData.category() != null) {
            try {
                template.category = TemplateCategory.valueOf(importData.category());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        if (importData.osType() != null) {
            try {
                template.osType = OsType.valueOf(importData.osType());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        template.archSupport = importData.archSupport();
        template.installScript = importData.installScript();
        template.configTemplate = importData.configTemplate();
        template.dependencies = importData.dependencies();
        template.source = source;
        template.deploymentCount = 0;
        template.successCount = 0;

        if (importData.commands() != null) {
            template.commands = new ArrayList<>();
            for (AgentCommandRecord.Create cmdCreate : importData.commands()) {
                AgentCommand command = new AgentCommand();
                command.name = cmdCreate.name();
                command.command = cmdCreate.command();
                command.description = cmdCreate.description();
                if (cmdCreate.category() != null) {
                    try {
                        command.category = com.easystation.agent.domain.enums.CommandCategory.valueOf(cmdCreate.category());
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
                command.order = cmdCreate.order() != null ? cmdCreate.order() : 0;
                command.template = template;
                template.commands.add(command);
            }
        }

        template.persist();
        return toRecord(template);
    }

    private AgentTemplateRecord toRecord(AgentTemplate t) {
        List<AgentCommandRecord> commands = t.commands != null ?
                t.commands.stream()
                        .sorted((a, b) -> Integer.compare(a.order != null ? a.order : 0, b.order != null ? b.order : 0))
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
                t.source != null ? new AgentSourceRecord(
                        t.source.id,
                        t.source.name,
                        t.source.type != null ? t.source.type.name() : null,
                        t.source.url,
                        t.source.description,
                        t.source.createdAt
                ) : null,
                commands,
                t.deploymentCount,
                t.successCount,
                successRate,
                t.createdAt,
                t.updatedAt
        );
    }
}