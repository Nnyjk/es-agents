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
    public String exportToJson(UUID templateId) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        try {
            Map<String, Object> exportData = toExportData(template);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to export template: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 导出模板为YAML格式
     */
    public String exportToYaml(UUID templateId) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        try {
            Map<String, Object> exportData = toExportData(template);
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to export template: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从JSON导入模板
     */
    @Transactional
    public AgentTemplateRecord importFromJson(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            return importFromData(data);
        } catch (IOException e) {
            throw new WebApplicationException("Invalid JSON format: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 从YAML导入模板
     */
    @Transactional
    public AgentTemplateRecord importFromYaml(String yaml) {
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yamlMapper.readValue(yaml, Map.class);
            return importFromData(data);
        } catch (IOException e) {
            throw new WebApplicationException("Invalid YAML format: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 从输入流导入
     */
    @Transactional
    public AgentTemplateRecord importFromStream(InputStream inputStream, String format) {
        try {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            if ("yaml".equalsIgnoreCase(format) || "yml".equalsIgnoreCase(format)) {
                return importFromYaml(content);
            } else {
                return importFromJson(content);
            }
        } catch (IOException e) {
            throw new WebApplicationException("Failed to read import file: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    private Map<String, Object> toExportData(AgentTemplate template) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", template.name);
        data.put("description", template.description);
        data.put("category", template.category != null ? template.category.name() : null);
        data.put("osType", template.osType != null ? template.osType.name() : null);
        data.put("archSupport", template.archSupport);
        data.put("installScript", template.installScript);
        data.put("configTemplate", template.configTemplate);
        data.put("dependencies", template.dependencies);

        // Export commands
        if (template.commands != null && !template.commands.isEmpty()) {
            List<Map<String, Object>> commands = template.commands.stream()
                    .map(cmd -> {
                        Map<String, Object> cmdData = new HashMap<>();
                        cmdData.put("name", cmd.name);
                        cmdData.put("script", cmd.script);
                        cmdData.put("timeout", cmd.timeout);
                        cmdData.put("defaultArgs", cmd.defaultArgs);
                        return cmdData;
                    })
                    .collect(Collectors.toList());
            data.put("commands", commands);
        }

        // Export version info
        data.put("exportedAt", java.time.LocalDateTime.now().toString());
        data.put("exportVersion", "1.0");

        return data;
    }

    @SuppressWarnings("unchecked")
    private AgentTemplateRecord importFromData(Map<String, Object> data) {
        String name = (String) data.get("name");
        if (name == null || name.isBlank()) {
            throw new WebApplicationException("Template name is required", Response.Status.BAD_REQUEST);
        }

        // Check for duplicate name
        if (AgentTemplate.find("name", name).firstResult() != null) {
            // Append timestamp to make it unique
            name = name + "_" + System.currentTimeMillis();
        }

        AgentTemplate template = new AgentTemplate();
        template.name = name;
        template.description = (String) data.get("description");

        if (data.get("category") != null) {
            try {
                template.category = TemplateCategory.valueOf(((String) data.get("category")).toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        if (data.get("osType") != null) {
            try {
                template.osType = OsType.valueOf(((String) data.get("osType")).toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        template.archSupport = (String) data.get("archSupport");
        template.installScript = (String) data.get("installScript");
        template.configTemplate = (String) data.get("configTemplate");
        template.dependencies = (String) data.get("dependencies");
        template.deploymentCount = 0;
        template.successCount = 0;

        // Import commands
        Object commandsObj = data.get("commands");
        if (commandsObj instanceof List) {
            List<Map<String, Object>> commandsData = (List<Map<String, Object>>) commandsObj;
            template.commands = new ArrayList<>();
            for (Map<String, Object> cmdData : commandsData) {
                AgentCommand cmd = new AgentCommand();
                cmd.name = (String) cmdData.get("name");
                cmd.script = (String) cmdData.get("script");
                cmd.timeout = cmdData.get("timeout") != null ? ((Number) cmdData.get("timeout")).longValue() : 60L;
                cmd.defaultArgs = (String) cmdData.get("defaultArgs");
                cmd.template = template;
                template.commands.add(cmd);
            }
        }

        // Handle source reference (if provided)
        Object sourceIdObj = data.get("sourceId");
        if (sourceIdObj != null) {
            UUID sourceId = sourceIdObj instanceof UUID ? (UUID) sourceIdObj : UUID.fromString((String) sourceIdObj);
            AgentSource source = AgentSource.findById(sourceId);
            if (source != null) {
                template.source = source;
            }
        }

        template.persist();

        // Convert to record
        List<AgentCommandRecord> commands = template.commands != null ?
                template.commands.stream()
                        .map(cmd -> new AgentCommandRecord(
                                cmd.id,
                                cmd.name,
                                cmd.script,
                                cmd.timeout,
                                cmd.defaultArgs,
                                template.id
                        ))
                        .collect(Collectors.toList()) :
                List.of();

        return new AgentTemplateRecord(
                template.id,
                template.name,
                template.description,
                template.category != null ? template.category.name() : null,
                template.osType != null ? template.osType.name() : null,
                template.archSupport,
                template.installScript,
                template.configTemplate,
                template.dependencies,
                null, // source - not exported/imported
                commands,
                template.deploymentCount,
                template.successCount,
                null,
                template.createdAt,
                template.updatedAt
        );
    }
}