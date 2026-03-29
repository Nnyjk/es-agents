package com.easystation.agent.planning.domain;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 任务规划实体
 * 存储任务规划的基本信息，支持多层分解和依赖关系
 */
@Entity
@Table(name = "planning_task")
@Getter
@Setter
public class PlanningTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 目标 ID，关联同一目标的任务 */
    @Column(name = "goal_id")
    public UUID goalId;

    /** 任务描述 */
    @Column(columnDefinition = "TEXT", nullable = false)
    public String description;

    /** 任务状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PlanningTaskStatus status = PlanningTaskStatus.CREATED;

    /** 任务优先级数值 (1-100) */
    @Column(name = "priority_value")
    public Integer priorityValue;

    /** 优先级枚举 */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    public TaskPriority priority = TaskPriority.NORMAL;

    /** 父任务 ID，用于多层分解 */
    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    public PlanningTask parentTask;

    /** 任务深度层级，根任务为 0 */
    @Column(name = "depth")
    public Integer depth = 0;

    /** 预估执行时间（秒） */
    @Column(name = "estimated_duration_seconds")
    public Long estimatedDurationSeconds;

    /** 实际执行时间（秒） */
    @Column(name = "actual_duration_seconds")
    public Long actualDurationSeconds;

    /** 任务执行参数 (JSON 格式) */
    @Column(columnDefinition = "TEXT")
    public String parameters;

    /** 任务执行结果 */
    @Column(columnDefinition = "TEXT")
    public String result;

    /** 错误信息 */
    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    /** 重试次数 */
    @Column(name = "retry_count")
    public Integer retryCount = 0;

    /** 最大重试次数 */
    @Column(name = "max_retry_count")
    public Integer maxRetryCount = 3;

    /** 执行器类型标识 */
    @Column(name = "executor_type")
    public String executorType;

    /** 创建时间 */
    @CreationTimestamp
    public LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    public LocalDateTime updatedAt;

    /** 开始执行时间 */
    @Column(name = "started_at")
    public LocalDateTime startedAt;

    /** 完成时间 */
    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    /**
     * 计算优先级分数
     * 考虑深度、依赖数量等因素
     */
    public int calculatePriorityScore() {
        int base = priority.getValue();
        // 深度越浅优先级越高
        int depthBonus = Math.max(0, 10 - depth * 2);
        return base + depthBonus;
    }

    /**
     * 是否可以执行
     */
    public boolean canExecute() {
        return status == PlanningTaskStatus.SCHEDULED || status == PlanningTaskStatus.RETRYING;
    }

    /**
     * 是否已完成（成功或失败）
     */
    public boolean isFinished() {
        return status == PlanningTaskStatus.COMPLETED
                || status == PlanningTaskStatus.FAILED
                || status == PlanningTaskStatus.CANCELLED;
    }

    /**
     * 是否可以取消
     */
    public boolean canCancel() {
        return status != PlanningTaskStatus.COMPLETED
                && status != PlanningTaskStatus.CANCELLED
                && status != PlanningTaskStatus.FAILED;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return status == PlanningTaskStatus.FAILED && retryCount < maxRetryCount;
    }
}