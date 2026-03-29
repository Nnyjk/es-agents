package com.easystation.agent.planning.domain;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 任务执行日志实体
 * 记录任务状态变更和执行过程
 */
@Entity
@Table(name = "planning_task_execution_log")
@Getter
@Setter
public class PlanningTaskExecutionLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 关联的任务 */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    public PlanningTask task;

    /** 状态变更前的状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    public PlanningTaskStatus fromStatus;

    /** 状态变更后的状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    public PlanningTaskStatus toStatus;

    /** 日志消息 */
    @Column(columnDefinition = "TEXT")
    public String message;

    /** 执行者 */
    @Column(name = "executed_by")
    public String executedBy;

    /** 创建时间 */
    @CreationTimestamp
    public LocalDateTime createdAt;

    /**
     * 创建状态变更日志
     */
    public static PlanningTaskExecutionLog create(PlanningTask task, PlanningTaskStatus fromStatus,
                                                   PlanningTaskStatus toStatus, String message, String executedBy) {
        PlanningTaskExecutionLog log = new PlanningTaskExecutionLog();
        log.task = task;
        log.fromStatus = fromStatus;
        log.toStatus = toStatus;
        log.message = message;
        log.executedBy = executedBy;
        return log;
    }
}