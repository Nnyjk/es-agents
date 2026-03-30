package com.easystation.agent.tool.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 工具执行日志实体
 * 记录工具调用的执行历史和结果
 */
@Entity
@Table(name = "agent_tool_execution_log")
@Getter
@Setter
public class ToolExecutionLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 工具 ID */
    @Column(nullable = false, length = 128)
    public String toolId;

    /** 关联的任务 ID */
    @Column(name = "task_id", length = 128)
    public String taskId;

    /** 输入参数（JSON 格式） */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String input;

    /** 输出结果（JSON 格式） */
    @Column(columnDefinition = "TEXT")
    public String output;

    /** 错误信息 */
    @Column(columnDefinition = "TEXT")
    public String error;

    /** 执行状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ToolExecutionStatus status;

    /** 执行耗时（毫秒） */
    @Column(nullable = false)
    public long durationMs;

    /** 执行时间 */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime executedAt;

    /**
     * 根据工具 ID 查找最近的执行日志
     */
    public static java.util.List<ToolExecutionLog> findByToolId(String toolId) {
        return find("toolId = ?1 order by executedAt desc", toolId).list();
    }

    /**
     * 根据任务 ID 查找执行日志
     */
    public static java.util.List<ToolExecutionLog> findByTaskId(String taskId) {
        return find("taskId = ?1 order by executedAt desc", taskId).list();
    }

    /**
     * 查找失败的执行日志
     */
    public static java.util.List<ToolExecutionLog> findFailed() {
        return find("status", ToolExecutionStatus.FAILED).list();
    }
}
