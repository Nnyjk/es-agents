package com.easystation.agent.tool.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 工具参数实体
 * 存储工具的参数定义和验证规则
 */
@Entity
@Table(name = "agent_tool_parameter")
@Getter
@Setter
public class ToolParameter extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 关联的工具定义 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_definition_id", nullable = false)
    public ToolDefinition tool;

    /** 参数名 */
    @Column(nullable = false, length = 128)
    public String name;

    /** 参数类型 (string, number, boolean, object, array) */
    @Column(nullable = false, length = 32)
    public String type;

    /** 参数描述 */
    @Column(columnDefinition = "TEXT")
    public String description;

    /** 是否必填 */
    @Column(nullable = false)
    public boolean required;

    /** 默认值（JSON 格式） */
    @Column(columnDefinition = "TEXT")
    public String defaultValue;

    /** 验证规则（JSON Schema） */
    @Column(columnDefinition = "TEXT")
    public String validationRule;

    /** 参数顺序 */
    @Column(nullable = false)
    public int order;

    /**
     * 根据工具查找所有参数（按顺序排序）
     */
    public static java.util.List<ToolParameter> findByTool(ToolDefinition tool) {
        return find("tool order by order asc", tool).list();
    }

    /**
     * 根据工具 ID 查找所有参数
     */
    public static java.util.List<ToolParameter> findByToolId(UUID toolId) {
        return find("tool.id order by order asc", toolId).list();
    }
}
