package com.easystation.agent.planning.repository;

import com.easystation.agent.planning.domain.PlanningTaskDependency;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * 任务依赖关系 Repository
 * 提供任务依赖关系的数据访问操作
 */
@ApplicationScoped
public class PlanningTaskDependencyRepository implements PanacheRepository<PlanningTaskDependency> {

    /**
     * 查询任务的所有依赖（该任务依赖哪些任务）
     */
    public List<PlanningTaskDependency> findByTaskId(UUID taskId) {
        return list("task.id", taskId);
    }

    /**
     * 查询任务的所有被依赖（哪些任务依赖该任务）
     */
    public List<PlanningTaskDependency> findByDependsOnTaskId(UUID dependsOnTaskId) {
        return list("dependsOnTask.id", dependsOnTaskId);
    }

    /**
     * 查询依赖关系的数量
     */
    public long countByTaskId(UUID taskId) {
        return count("task.id", taskId);
    }

    /**
     * 查询被依赖的数量
     */
    public long countByDependsOnTaskId(UUID dependsOnTaskId) {
        return count("dependsOnTask.id", dependsOnTaskId);
    }

    /**
     * 检查依赖关系是否存在
     */
    public boolean existsDependency(UUID taskId, UUID dependsOnTaskId) {
        return count("task.id = ?1 and dependsOnTask.id = ?2", taskId, dependsOnTaskId) > 0;
    }

    /**
     * 删除任务的所有依赖关系
     */
    public long deleteByTaskId(UUID taskId) {
        return delete("task.id", taskId);
    }

    /**
     * 删除任务的所有被依赖关系
     */
    public long deleteByDependsOnTaskId(UUID dependsOnTaskId) {
        return delete("dependsOnTask.id", dependsOnTaskId);
    }

    /**
     * 删除特定依赖关系
     */
    public long deleteDependency(UUID taskId, UUID dependsOnTaskId) {
        return delete("task.id = ?1 and dependsOnTask.id = ?2", taskId, dependsOnTaskId);
    }
}