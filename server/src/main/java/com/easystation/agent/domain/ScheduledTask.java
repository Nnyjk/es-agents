package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.ScheduledTaskCategory;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 定时任务实体类
 * 用于定义和管理定时任务的调度配置
 */
@Entity
@Table(name = "scheduled_task", indexes = {
    @Index(name = "idx_scheduled_task_category", columnList = "category"),
    @Index(name = "idx_scheduled_task_active", columnList = "is_active"),
    @Index(name = "idx_scheduled_task_next_exec", columnList = "next_execution_at")
})
@Getter
@Setter
public class ScheduledTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 任务名称
     */
    @Column(nullable = false)
    public String name;

    /**
     * 任务描述
     */
    @Column(columnDefinition = "TEXT")
    public String description;

    /**
     * 执行脚本/命令
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    public String script;

    /**
     * Cron 表达式
     */
    @Column(nullable = false)
    public String cronExpression;

    /**
     * 任务分类
     */
    @Enumerated(EnumType.STRING)
    public ScheduledTaskCategory category = ScheduledTaskCategory.CUSTOM;

    /**
     * 标签 (JSON 数组)
     */
    @Column(columnDefinition = "TEXT")
    public String tags;

    /**
     * 执行超时时间 (秒)
     */
    @Column(nullable = false)
    public Long timeout = 300L;

    /**
     * 重试次数
     */
    @Column(nullable = false)
    public Integer retryCount = 0;

    /**
     * 重试间隔 (秒)
     */
    @Column(nullable = false)
    public Long retryInterval = 60L;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    public Boolean isActive = true;

    /**
     * 默认执行参数 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    public String parameters;

    /**
     * 最后执行时间
     */
    @Column
    public LocalDateTime lastExecutedAt;

    /**
     * 下次执行时间
     */
    @Column
    public LocalDateTime nextExecutionAt;

    /**
     * 创建人
     */
    @Column
    public String createdBy;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    public LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    @UpdateTimestamp
    public LocalDateTime updatedAt;

    /**
     * 查找所有启用的任务
     *
     * @return 启用的任务列表
     */
    public static java.util.List<ScheduledTask> listActive() {
        return find("isActive = true", true).list();
    }

    /**
     * 按分类查找任务
     *
     * @param category 任务分类
     * @return 任务列表
     */
    public static java.util.List<ScheduledTask> findByCategory(ScheduledTaskCategory category) {
        return find("category", category).list();
    }

    /**
     * 按名称查找任务
     *
     * @param name 任务名称
     * @return 任务
     */
    public static ScheduledTask findByName(String name) {
        return find("name", name).firstResult();
    }

    /**
     * 查找需要执行的任务 (下次执行时间 <= 当前时间)
     *
     * @return 待执行任务列表
     */
    public static java.util.List<ScheduledTask> findDueTasks() {
        return find("isActive = true and nextExecutionAt <= ?1", LocalDateTime.now()).list();
    }
}
