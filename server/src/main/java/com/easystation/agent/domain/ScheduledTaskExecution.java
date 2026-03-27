package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.ExecutionStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 定时任务执行记录实体类
 * 用于记录每次定时任务的执行情况
 */
@Entity
@Table(name = "scheduled_task_execution", indexes = {
    @Index(name = "idx_task_exec_status", columnList = "status"),
    @Index(name = "idx_task_exec_scheduled_task", columnList = "scheduled_task_id"),
    @Index(name = "idx_task_exec_created", columnList = "created_at")
})
@Getter
@Setter
public class ScheduledTaskExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 关联的定时任务
     */
    @ManyToOne
    @JoinColumn(name = "scheduled_task_id", nullable = false)
    public ScheduledTask scheduledTask;

    /**
     * 执行节点 ID
     */
    @Column(name = "agent_instance_id")
    public UUID agentInstanceId;

    /**
     * 执行状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExecutionStatus status = ExecutionStatus.PENDING;

    /**
     * 开始时间
     */
    @Column
    public LocalDateTime startedAt;

    /**
     * 结束时间
     */
    @Column
    public LocalDateTime finishedAt;

    /**
     * 执行输出
     */
    @Column(columnDefinition = "TEXT")
    public String output;

    /**
     * 退出码
     */
    @Column
    public Integer exitCode;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    /**
     * 当前重试次数
     */
    @Column(nullable = false)
    public Integer retryCount = 0;

    /**
     * 触发方式 (SCHEDULED/MANUAL/API)
     */
    @Column(nullable = false)
    public String triggeredBy = "SCHEDULED";

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    public LocalDateTime createdAt;

    /**
     * 按任务 ID 查找执行记录
     *
     * @param taskId 任务 ID
     * @return 执行记录列表
     */
    public static java.util.List<ScheduledTaskExecution> findByTaskId(UUID taskId) {
        return find("scheduledTask.id", taskId).list();
    }

    /**
     * 按任务 ID 和状态查找执行记录
     *
     * @param taskId 任务 ID
     * @param status 执行状态
     * @return 执行记录列表
     */
    public static java.util.List<ScheduledTaskExecution> findByTaskIdAndStatus(UUID taskId, ExecutionStatus status) {
        return find("scheduledTask.id = ?1 and status = ?2", taskId, status).list();
    }

    /**
     * 查找正在运行的执行记录
     *
     * @param taskId 任务 ID
     * @return 执行记录
     */
    public static ScheduledTaskExecution findRunningByTaskId(UUID taskId) {
        return find("scheduledTask.id = ?1 and status = ?2", taskId, ExecutionStatus.RUNNING).firstResult();
    }

    /**
     * 按状态查找执行记录
     *
     * @param status 执行状态
     * @return 执行记录列表
     */
    public static java.util.List<ScheduledTaskExecution> findByStatus(ExecutionStatus status) {
        return find("status", status).list();
    }
}
