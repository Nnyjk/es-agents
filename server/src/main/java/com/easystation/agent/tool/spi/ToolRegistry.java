package com.easystation.agent.tool.spi;

import java.util.List;
import java.util.Optional;

/**
 * 工具注册表接口
 * 用于工具的注册、发现和查找
 */
public interface ToolRegistry {

    /**
     * 注册工具
     * @param tool 工具实例
     */
    void register(Tool tool);

    /**
     * 注销工具
     * @param toolId 工具 ID
     */
    void unregister(String toolId);

    /**
     * 获取工具
     * @param toolId 工具 ID
     * @return 工具实例（可能为空）
     */
    Optional<Tool> getTool(String toolId);

    /**
     * 列出所有已注册的工具
     * @return 工具列表
     */
    List<Tool> listTools();

    /**
     * 列出所有已启用的工具
     * @return 工具列表
     */
    List<Tool> listEnabledTools();

    /**
     * 搜索工具
     * @param query 搜索关键词
     * @return 匹配的工具列表
     */
    List<Tool> searchTools(String query);

    /**
     * 根据分类查找工具
     * @param category 分类名称
     * @return 工具列表
     */
    List<Tool> getToolsByCategory(String category);

    /**
     * 检查工具是否已注册
     * @param toolId 工具 ID
     * @return 是否已注册
     */
    boolean isRegistered(String toolId);

    /**
     * 获取已注册工具数量
     * @return 工具数量
     */
    int getToolCount();
}
