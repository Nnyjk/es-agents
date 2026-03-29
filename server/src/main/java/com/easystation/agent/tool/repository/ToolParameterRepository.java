package com.easystation.agent.tool.repository;

import com.easystation.agent.tool.domain.ToolParameter;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * 工具参数数据访问层
 */
@ApplicationScoped
public class ToolParameterRepository implements PanacheRepository<ToolParameter> {

    /**
     * 根据工具查找所有参数（按顺序排序）
     */
    public List<ToolParameter> findByTool(UUID toolId) {
        return find("tool.id order by order asc", toolId).list();
    }

    /**
     * 根据工具 ID 查找所有参数
     */
    public List<ToolParameter> findByToolId(UUID toolId) {
        return findByTool(toolId);
    }

    /**
     * 删除工具的所有参数
     */
    public void deleteByTool(UUID toolId) {
        delete("tool.id", toolId);
    }

    /**
     * 查找必填参数
     */
    public List<ToolParameter> findRequiredByTool(UUID toolId) {
        return find("tool.id = ?1 and required = true order by order asc", toolId).list();
    }

    /**
     * 根据参数名查找
     */
    public ToolParameter findByName(UUID toolId, String name) {
        return find("tool.id = ?1 and name = ?2", toolId, name).firstResult();
    }
}
