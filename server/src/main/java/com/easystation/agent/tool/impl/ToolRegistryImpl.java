package com.easystation.agent.tool.impl;

import com.easystation.agent.tool.domain.ToolDefinition;
import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.domain.ToolStatus;
import com.easystation.agent.tool.repository.ToolDefinitionRepository;
import com.easystation.agent.tool.repository.ToolParameterRepository;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolRegistry;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表实现
 * 支持内存缓存 + 数据库持久化
 */
@ApplicationScoped
public class ToolRegistryImpl implements ToolRegistry {

    @Inject
    ToolDefinitionRepository toolDefinitionRepository;

    @Inject
    ToolParameterRepository toolParameterRepository;

    /** 内存缓存：toolId -> Tool 实例 */
    private final Map<String, Tool> toolCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void register(Tool tool) {
        String toolId = tool.getId();

        // 检查是否已存在
        ToolDefinition existing = toolDefinitionRepository.findByToolId(toolId);
        if (existing != null) {
            Log.warnf("Tool %s already registered, updating...", toolId);
        }

        // 创建或更新工具定义
        ToolDefinition definition = new ToolDefinition();
        if (existing != null) {
            definition = existing;
        }
        definition.toolId = toolId;
        definition.name = tool.getName();
        definition.description = tool.getDescription();
        definition.category = extractCategory(toolId);
        definition.version = "1.0.0";
        definition.status = ToolStatus.ENABLED;

        toolDefinitionRepository.persist(definition);

        // 更新参数
        if (existing != null) {
            toolParameterRepository.deleteByTool(definition.id);
        }

        List<ToolParameter> parameters = tool.getParameters();
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                ToolParameter param = parameters.get(i);
                ToolParameter newParam = new ToolParameter();
                newParam.tool = definition;
                newParam.name = param.name;
                newParam.type = param.type;
                newParam.description = param.description;
                newParam.required = param.required;
                newParam.defaultValue = param.defaultValue;
                newParam.validationRule = param.validationRule;
                newParam.order = param.order != 0 ? param.order : i;
                toolParameterRepository.persist(newParam);
            }
        }

        // 更新缓存
        toolCache.put(toolId, tool);
        Log.infof("Tool %s registered successfully", toolId);
    }

    @Override
    @Transactional
    public void unregister(String toolId) {
        ToolDefinition definition = toolDefinitionRepository.findByToolId(toolId);
        if (definition != null) {
            // 设置为禁用状态而非删除
            definition.status = ToolStatus.DEPRECATED;
            toolDefinitionRepository.persist(definition);
            Log.infof("Tool %s unregistered (marked as deprecated)", toolId);
        }

        // 移除缓存
        toolCache.remove(toolId);
    }

    @Override
    public Optional<Tool> getTool(String toolId) {
        // 先从缓存获取
        Tool cached = toolCache.get(toolId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // 从数据库加载
        ToolDefinition definition = toolDefinitionRepository.findByToolId(toolId);
        if (definition == null || definition.status != ToolStatus.ENABLED) {
            return Optional.empty();
        }

        // 通过 SPI 加载工具实例
        ServiceLoader<Tool> loader = ServiceLoader.load(Tool.class);
        for (Tool tool : loader) {
            if (toolId.equals(tool.getId())) {
                toolCache.put(toolId, tool);
                return Optional.of(tool);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Tool> listTools() {
        List<Tool> tools = new ArrayList<>();
        toolDefinitionRepository.findAll()
                .stream()
                .filter(d -> d.status == ToolStatus.ENABLED)
                .forEach(d -> getTool(d.toolId).ifPresent(tools::add));
        return tools;
    }

    @Override
    public List<Tool> listEnabledTools() {
        return listTools();
    }

    @Override
    public List<Tool> searchTools(String query) {
        List<Tool> tools = new ArrayList<>();
        toolDefinitionRepository.search(query)
                .stream()
                .filter(d -> d.status == ToolStatus.ENABLED)
                .forEach(d -> getTool(d.toolId).ifPresent(tools::add));
        return tools;
    }

    @Override
    public List<Tool> getToolsByCategory(String category) {
        List<Tool> tools = new ArrayList<>();
        toolDefinitionRepository.findByCategory(category)
                .stream()
                .filter(d -> d.status == ToolStatus.ENABLED)
                .forEach(d -> getTool(d.toolId).ifPresent(tools::add));
        return tools;
    }

    @Override
    public boolean isRegistered(String toolId) {
        return toolCache.containsKey(toolId) ||
               toolDefinitionRepository.existsByToolId(toolId);
    }

    @Override
    public int getToolCount() {
        return (int) toolDefinitionRepository.count("status", ToolStatus.ENABLED);
    }

    /**
     * 从 toolId 提取分类
     * 如：shell.execute -> shell
     */
    private String extractCategory(String toolId) {
        int dotIndex = toolId.indexOf('.');
        if (dotIndex > 0) {
            return toolId.substring(0, dotIndex);
        }
        return "unknown";
    }
}
