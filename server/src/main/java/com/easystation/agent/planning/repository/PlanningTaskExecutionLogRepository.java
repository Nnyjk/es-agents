package com.easystation.agent.planning.repository;

import com.easystation.agent.planning.domain.PlanningTaskExecutionLog;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 任务执行日志 Repository
 * 提供任务执行日志的数据访问操作
 */
@ApplicationScoped
public class PlanningTaskExecutionLogRepository implements PanacheRepositoryBase<PlanningTaskExecutionLog, UUID> {

    /**
     * 查询任务的执行日志（按时间倒序）
     */
    public List<PlanningTaskExecutionLog> findByTaskIdOrderByCreatedAtDesc(UUID taskId) {
        return list("task.id = ?1 order by createdAt desc", taskId);
    }

    /**
     * 查询任务最近 N 条执行日志
     */
    public List<PlanningTaskExecutionLog> findRecentLogsByTaskId(UUID taskId, int limit) {
        return find("task.id = ?1 order by createdAt desc", taskId)
                .range(0, limit - 1)
                .list();
    }

    /**
     * 查询指定状态变更的日志
     */
    public List<PlanningTaskExecutionLog> findByToStatus(PlanningTaskStatus toStatus) {
        return list("toStatus", toStatus);
    }

    /**
     * 查询指定时间范围内的日志
     */
    public List<PlanningTaskExecutionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return list("createdAt between ?1 and ?2 order by createdAt desc", start, end);
    }

    /**
     * 查询指定执行者的日志
     */
    public List<PlanningTaskExecutionLog> findByExecutedBy(String executedBy) {
        return list("executedBy = ?1 order by createdAt desc", executedBy);
    }

    /**
     * 统计任务的日志数量
     */
    public long countByTaskId(UUID taskId) {
        return count("task.id", taskId);
    }

    /**
     * 删除任务的执行日志
     */
    public long deleteByTaskId(UUID taskId) {
        return delete("task.id", taskId);
    }

    /**
     * 删除指定时间之前的日志
     */
    public long deleteByCreatedAtBefore(LocalDateTime date) {
        return delete("createdAt < ?1", date);
    }
}