package com.easystation.agent.planning.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 任务依赖关系实体
 * 定义任务之间的依赖关系
 */
@Entity
@Table(name = "planning_task_dependency")
@Getter
@Setter
public class PlanningTaskDependency extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 被依赖的任务（前置任务） */
    @ManyToOne
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    public PlanningTask dependsOnTask;

    /** 依赖的任务（后置任务） */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    public PlanningTask task;

    /** 依赖类型：HARD（必须完成）或 SOFT（期望完成） */
    @Column(name = "dependency_type", nullable = false)
    public String dependencyType = "HARD";

    /**
     * 创建依赖关系
     */
    public static PlanningTaskDependency create(PlanningTask task, PlanningTask dependsOnTask) {
        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task;
        dep.dependsOnTask = dependsOnTask;
        dep.dependencyType = "HARD";
        return dep;
    }
}