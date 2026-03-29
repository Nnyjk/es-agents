package com.easystation.agent.tool.repository;

import com.easystation.agent.tool.domain.ToolDefinition;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * 工具定义数据访问层
 */
@ApplicationScoped
public class ToolDefinitionRepository implements PanacheRepository<ToolDefinition> {

    /**
     * 根据 toolId 查找工具
     */
    public ToolDefinition findByToolId(String toolId) {
        return find("toolId", toolId).firstResult();
    }

    /**
     * 查找所有已启用的工具
     */
    public List<ToolDefinition> findEnabled() {
        return find("status", com.easystation.agent.tool.domain.ToolStatus.ENABLED).list();
    }

    /**
     * 根据分类查找工具
     */
    public List<ToolDefinition> findByCategory(String category) {
        return find("category", category).list();
    }

    /**
     * 搜索工具（按名称或描述）
     */
    public List<ToolDefinition> search(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";
        return find("lower(name) like ?1 or lower(description) like ?1", searchPattern).list();
    }

    /**
     * 检查 toolId 是否存在
     */
    public boolean existsByToolId(String toolId) {
        return count("toolId", toolId) > 0;
    }

    /**
     * 更新工具状态
     */
    public void updateStatus(UUID id, com.easystation.agent.tool.domain.ToolStatus status) {
        ToolDefinition tool = findById(id);
        if (tool != null) {
            tool.status = status;
            persist(tool);
        }
    }
}
