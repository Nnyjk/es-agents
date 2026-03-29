package com.easystation.agent.tool.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 工具定义实体
 * 存储工具的基本信息和元数据
 */
@Entity
@Table(name = "agent_tool_definition")
@Getter
@Setter
public class ToolDefinition extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 工具唯一标识（如：shell.execute） */
    @Column(nullable = false, unique = true, length = 128)
    public String toolId;

    /** 工具名称 */
    @Column(nullable = false, length = 128)
    public String name;

    /** 工具描述 */
    @Column(columnDefinition = "TEXT")
    public String description;

    /** 工具分类 */
    @Column(length = 64)
    public String category;

    /** 版本号 */
    @Column(length = 32)
    public String version;

    /** 工具状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ToolStatus status;

    /** 创建时间 */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(nullable = false)
    public LocalDateTime updatedAt;

    /**
     * 查找所有已启用的工具
     */
    public static java.util.List<ToolDefinition> findEnabled() {
        return find("status", ToolStatus.ENABLED).list();
    }

    /**
     * 根据 toolId 查找工具
     */
    public static ToolDefinition findByToolId(String toolId) {
        return find("toolId", toolId).firstResult();
    }

    /**
     * 根据分类查找工具
     */
    public static java.util.List<ToolDefinition> findByCategory(String category) {
        return find("category", category).list();
    }
}
