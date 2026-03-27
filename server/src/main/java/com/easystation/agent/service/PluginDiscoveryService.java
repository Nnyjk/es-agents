package com.easystation.agent.service;

import com.easystation.agent.dto.PluginInfoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
public class PluginDiscoveryService {

    // 存储 Agent 的插件列表
    private final Map<String, List<PluginInfoDTO>> agentPlugins = new ConcurrentHashMap<>();

    /**
     * 注册 Agent 的插件列表
     * 心跳上报时更新插件状态
     */
    public void registerPlugins(String agentId, List<PluginInfoDTO> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            log.debug("Agent {} has no plugins to register", agentId);
            return;
        }
        agentPlugins.put(agentId, plugins);
        log.info("Registered {} plugins for agent {}", plugins.size(), agentId);
    }

    /**
     * 获取 Agent 的所有插件
     */
    public List<PluginInfoDTO> getPlugins(String agentId) {
        return agentPlugins.getOrDefault(agentId, new ArrayList<>());
    }

    /**
     * 获取 Agent 的特定插件
     */
    public Optional<PluginInfoDTO> getPlugin(String agentId, String pluginId) {
        List<PluginInfoDTO> plugins = agentPlugins.getOrDefault(agentId, new ArrayList<>());
        return plugins.stream()
            .filter(p -> p.id().equals(pluginId))
            .findFirst();
    }

    /**
     * 更新插件状态
     */
    public void updatePluginStatus(String agentId, String pluginId, String status) {
        List<PluginInfoDTO> plugins = agentPlugins.get(agentId);
        if (plugins == null) {
            log.warn("Agent {} not found in plugin registry", agentId);
            return;
        }

        for (int i = 0; i < plugins.size(); i++) {
            PluginInfoDTO old = plugins.get(i);
            if (old.id().equals(pluginId)) {
                PluginInfoDTO updated = new PluginInfoDTO(
                    old.id(),
                    old.name(),
                    old.version(),
                    old.description(),
                    old.capabilities(),
                    status,
                    old.registeredAt(),
                    java.time.Instant.now()
                );
                plugins.set(i, updated);
                log.info("Updated plugin {} status to {} for agent {}", pluginId, status, agentId);
                return;
            }
        }
        log.warn("Plugin {} not found for agent {}", pluginId, agentId);
    }

    /**
     * 移除 Agent 的所有插件
     * Agent 下线时调用
     */
    public void removeAgent(String agentId) {
        agentPlugins.remove(agentId);
        log.info("Removed all plugins for agent {}", agentId);
    }

    /**
     * 获取所有 Agent 的插件统计
     */
    public Map<String, List<PluginInfoDTO>> getAllPlugins() {
        return new ConcurrentHashMap<>(agentPlugins);
    }

    /**
     * 获取运行中的插件数量
     */
    public long countRunningPlugins(String agentId) {
        List<PluginInfoDTO> plugins = agentPlugins.getOrDefault(agentId, new ArrayList<>());
        return plugins.stream()
            .filter(p -> "RUNNING".equals(p.status()))
            .count();
    }
}