package com.easystation.agent.planning.repository;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 任务规划 Repository
 * 提供任务规划相关的数据访问操作
 */
@ApplicationScoped
public class PlanningTaskRepository implements PanacheRepositoryBase<PlanningTask, UUID> {

    /**
     * 根据目标 ID 查询所有任务
     */
    public List<PlanningTask> findByGoalId(UUID goalId) {
        return list("goalId", goalId);
    }

    /**
     * 根据目标 ID 和状态查询任务
     */
    public List<PlanningTask> findByGoalIdAndStatus(UUID goalId, PlanningTaskStatus status) {
        return list("goalId = ?1 and status = ?2", goalId, status);
    }

    /**
     * 根据父任务 ID 查询子任务
     */
    public List<PlanningTask> findByParentTaskId(UUID parentTaskId) {
        return list("parentTask.id", parentTaskId);
    }

    /**
     * 根据状态查询任务
     */
    public List<PlanningTask> findByStatus(PlanningTaskStatus status) {
        return list("status", status);
    }

    /**
     * 查询指定状态的任务（按优先级排序）
     */
    public List<PlanningTask> findByStatusOrderByPriority(PlanningTaskStatus status) {
        return list("status = ?1 order by priorityValue desc, depth asc", status);
    }

    /**
     * 查询可执行的任务（SCHEDULED 或 RETRYING 状态）
     */
    public List<PlanningTask> findExecutableTasks() {
        return list("status in (?1, ?2) order by priorityValue desc, createdAt asc",
                PlanningTaskStatus.SCHEDULED, PlanningTaskStatus.RETRYING);
    }

    /**
     * 查询根任务（无父任务）
     */
    public List<PlanningTask> findRootTasks() {
        return list("parentTask is null");
    }

    /**
     * 查询指定目标的根任务
     */
    public List<PlanningTask> findRootTasksByGoalId(UUID goalId) {
        return list("goalId = ?1 and parentTask is null", goalId);
    }

    /**
     * 统计指定状态的任务数量
     */
    public long countByStatus(PlanningTaskStatus status) {
        return count("status", status);
    }

    /**
     * 统计指定目标的任务数量
     */
    public long countByGoalId(UUID goalId) {
        return count("goalId", goalId);
    }

    /**
     * 统计指定父任务的子任务数量
     */
    public long countByParentTaskId(UUID parentTaskId) {
        return count("parentTask.id", parentTaskId);
    }

    /**
     * 查询指定时间范围内创建的任务
     */
    public List<PlanningTask> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return list("createdAt between ?1 and ?2", start, end);
    }

    /**
     * 查询失败且可重试的任务
     */
    public List<PlanningTask> findRetryableTasks() {
        return list("status = ?1 and retryCount < maxRetryCount", PlanningTaskStatus.FAILED);
    }

    /**
     * 根据执行器类型查询任务
     */
    public List<PlanningTask> findByExecutorType(String executorType) {
        return list("executorType", executorType);
    }

    /**
     * 根据深度层级查询任务
     */
    public List<PlanningTask> findByDepth(int depth) {
        return list("depth", depth);
    }

    /**
     * 更新任务状态
     */
    public int updateStatus(UUID taskId, PlanningTaskStatus newStatus) {
        return update("status = ?1 where id = ?2", newStatus, taskId);
    }

    /**
     * 批量更新目标下所有任务状态
     */
    public int updateStatusByGoalId(UUID goalId, PlanningTaskStatus newStatus) {
        return update("status = ?1 where goalId = ?2", newStatus, goalId);
    }
}