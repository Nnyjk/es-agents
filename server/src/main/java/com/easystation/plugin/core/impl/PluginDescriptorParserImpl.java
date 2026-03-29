package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 插件描述符解析器实现（基于 Jackson）
 */
public class PluginDescriptorParserImpl implements PluginDescriptorParser {
    
    private final ObjectMapper objectMapper;
    
    public PluginDescriptorParserImpl() {
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public PluginDescriptor parse(InputStream inputStream) throws PluginException {
        try {
            JsonNode root = objectMapper.readTree(inputStream);
            return parseJsonNode(root);
        } catch (Exception e) {
            throw new PluginException("Failed to parse plugin.json", e);
        }
    }
    
    @Override
    public PluginDescriptor parse(Path pluginJsonPath) throws PluginException {
        try (InputStream inputStream = Files.newInputStream(pluginJsonPath)) {
            return parse(inputStream);
        } catch (PluginException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginException("Failed to read plugin.json: " + pluginJsonPath, e);
        }
    }
    
    private PluginDescriptor parseJsonNode(JsonNode root) throws PluginException {
        // 验证必填字段
        validateRequiredFields(root);
        
        return new PluginDescriptorImpl(
            getRequiredString(root, "id"),
            getRequiredString(root, "name"),
            getRequiredString(root, "version"),
            getOptionalString(root, "description", null),
            getOptionalString(root, "author", null),
            getOptionalString(root, "license", null),
            getRequiredString(root, "main"),
            parseDependencies(root.get("dependencies")),
            parseStringArray(root.get("provides")),
            parseStringArray(root.get("requires")),
            parseConfigSchema(root.get("config")),
            parseConfigDefaults(root.get("config"))
        );
    }
    
    private void validateRequiredFields(JsonNode root) throws PluginException {
        String[] requiredFields = {"id", "name", "version", "main"};
        for (String field : requiredFields) {
            if (!root.has(field)) {
                throw new PluginException("Missing required field: " + field);
            }
        }
        
        // 验证 ID 格式
        String id = root.get("id").asText();
        if (!id.matches("^[a-z][a-z0-9-]*$")) {
            throw new PluginException("Invalid plugin ID format: " + id + " (must be lowercase letters, numbers, and hyphens, starting with a letter)");
        }
        
        // 验证版本格式
        String version = root.get("version").asText();
        if (!version.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?$")) {
            throw new PluginException("Invalid version format: " + version + " (must be semantic version: major.minor.patch)");
        }
    }
    
    private String getRequiredString(JsonNode node, String field) throws PluginException {
        if (!node.has(field)) {
            throw new PluginException("Missing required field: " + field);
        }
        return node.get(field).asText();
    }
    
    private String getOptionalString(JsonNode node, String field, String defaultValue) {
        if (!node.has(field)) {
            return defaultValue;
        }
        return node.get(field).asText();
    }
    
    private List<PluginDescriptor.PluginDependency> parseDependencies(JsonNode node) {
        if (node == null || !node.isArray()) {
            return new ArrayList<>();
        }
        
        List<PluginDescriptor.PluginDependency> dependencies = new ArrayList<>();
        for (JsonNode depNode : node) {
            String id = depNode.has("id") ? depNode.get("id").asText() : null;
            String version = depNode.has("version") ? depNode.get("version").asText() : "*";
            
            if (id != null) {
                dependencies.add(new PluginDescriptor.PluginDependency(id, version));
            }
        }
        return dependencies;
    }
    
    private List<String> parseStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return new ArrayList<>();
        }
        
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                result.add(item.asText());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfigSchema(JsonNode configNode) {
        if (configNode == null || !configNode.has("schema")) {
            return new HashMap<>();
        }
        return objectMapper.convertValue(configNode.get("schema"), Map.class);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfigDefaults(JsonNode configNode) {
        if (configNode == null || !configNode.has("defaults")) {
            return new HashMap<>();
        }
        return objectMapper.convertValue(configNode.get("defaults"), Map.class);
    }
}
