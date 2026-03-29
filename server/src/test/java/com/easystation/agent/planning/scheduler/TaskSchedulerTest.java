package com.easystation.agent.planning.scheduler;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.engine.TaskGraph;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 任务调度器单元测试
 */
@QuarkusTest
class TaskSchedulerTest {

    @Inject
    TaskScheduler taskScheduler;

    @InjectMock
    PlanningTaskRepository taskRepository;

    @InjectMock
    PlanningTaskDependencyRepository dependencyRepository;

    @BeforeEach
    void setup() {
        Mockito.reset(taskRepository, dependencyRepository);
    }

    /**
     * 测试调度已完成任务（不应被调度）
     */
    @Test
    void testScheduleCompletedTask() {
        PlanningTask task = createTestTask("Completed Task", TaskPriority.NORMAL, PlanningTaskStatus.COMPLETED);
        task.id = UUID.randomUUID();

        PlanningTask scheduled = taskScheduler.scheduleTask(task);

        assertNull(scheduled);
    }

    /**
     * 测试依赖检查 - 无依赖
     */
    @Test
    void testAreDependenciesMetNoDependencies() {
        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();

        when(dependencyRepository.findByTaskId(task.id)).thenReturn(Collections.emptyList());

        boolean met = taskScheduler.areDependenciesMet(task.id);

        assertTrue(met);
    }

    /**
     * 测试依赖检查 - 依赖已完成
     */
    @Test
    void testAreDependenciesMetCompletedDependency() {
        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask depTask = createTestTask("Dependency", TaskPriority.NORMAL, PlanningTaskStatus.COMPLETED);

        task.id = UUID.randomUUID();
        depTask.id = UUID.randomUUID();

        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task;
        dep.dependsOnTask = depTask;
        dep.dependencyType = "HARD";

        when(dependencyRepository.findByTaskId(task.id)).thenReturn(List.of(dep));

        boolean met = taskScheduler.areDependenciesMet(task.id);

        assertTrue(met);
    }

    /**
     * 测试依赖检查 - 依赖未完成
     */
    @Test
    void testAreDependenciesMetUncompletedDependency() {
        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask depTask = createTestTask("Dependency", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);

        task.id = UUID.randomUUID();
        depTask.id = UUID.randomUUID();

        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task;
        dep.dependsOnTask = depTask;
        dep.dependencyType = "HARD";

        when(dependencyRepository.findByTaskId(task.id)).thenReturn(List.of(dep));

        boolean met = taskScheduler.areDependenciesMet(task.id);

        assertFalse(met);
    }

    /**
     * 测试软依赖不阻塞
     */
    @Test
    void testSoftDependencyDoesNotBlock() {
        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask depTask = createTestTask("Dependency", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);

        task.id = UUID.randomUUID();
        depTask.id = UUID.randomUUID();

        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task;
        dep.dependsOnTask = depTask;
        dep.dependencyType = "SOFT";

        when(dependencyRepository.findByTaskId(task.id)).thenReturn(List.of(dep));

        boolean met = taskScheduler.areDependenciesMet(task.id);

        assertTrue(met);
    }

    /**
     * 测试优先级计算
     */
    @Test
    void testCalculatePriorityScore() {
        PlanningTask urgentTask = createTestTask("Urgent", TaskPriority.URGENT, PlanningTaskStatus.SCHEDULED);
        urgentTask.id = UUID.randomUUID();
        urgentTask.depth = 0;
        urgentTask.retryCount = 0;

        PlanningTask normalTask = createTestTask("Normal", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        normalTask.id = UUID.randomUUID();
        normalTask.depth = 0;
        normalTask.retryCount = 0;

        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        int urgentScore = taskScheduler.calculatePriorityScore(urgentTask);
        int normalScore = taskScheduler.calculatePriorityScore(normalTask);

        assertTrue(urgentScore > normalScore);
        assertTrue(urgentScore >= TaskPriority.URGENT.getValue());
        assertTrue(normalScore >= TaskPriority.NORMAL.getValue());
    }

    /**
     * 测试深度对优先级的影响
     */
    @Test
    void testDepthAffectsPriority() {
        PlanningTask shallowTask = createTestTask("Shallow", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        shallowTask.id = UUID.randomUUID();
        shallowTask.depth = 0;

        PlanningTask deepTask = createTestTask("Deep", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        deepTask.id = UUID.randomUUID();
        deepTask.depth = 3;

        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        int shallowScore = taskScheduler.calculatePriorityScore(shallowTask);
        int deepScore = taskScheduler.calculatePriorityScore(deepTask);

        assertTrue(shallowScore > deepScore);
    }

    /**
     * 测试空目标调度
     */
    @Test
    void testScheduleEmptyGoal() {
        List<PlanningTask> scheduled = taskScheduler.scheduleGoal(null);
        assertTrue(scheduled.isEmpty());

        UUID goalId = UUID.randomUUID();
        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.READY))
                .thenReturn(Collections.emptyList());

        scheduled = taskScheduler.scheduleGoal(goalId);
        assertTrue(scheduled.isEmpty());
    }

    /**
     * 测试构建任务图 - 空目标
     */
    @Test
    void testBuildTaskGraphEmpty() {
        TaskGraph graph = taskScheduler.buildTaskGraph(null);
        assertNotNull(graph);
        assertTrue(graph.getTasks().isEmpty());
    }

    /**
     * 测试任务是否可执行
     */
    @Test
    void testCanExecute() {
        PlanningTask scheduledTask = createTestTask("Scheduled", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        scheduledTask.id = UUID.randomUUID();

        PlanningTask runningTask = createTestTask("Running", TaskPriority.NORMAL, PlanningTaskStatus.RUNNING);
        runningTask.id = UUID.randomUUID();

        PlanningTask nullTask = null;

        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        assertTrue(taskScheduler.canExecute(scheduledTask));
        assertFalse(taskScheduler.canExecute(runningTask));
        assertFalse(taskScheduler.canExecute(nullTask));
    }

    /**
     * 测试获取调度状态 - 空目标
     */
    @Test
    void testGetScheduleStatusNullGoal() {
        TaskScheduler.ScheduleStatus status = taskScheduler.getScheduleStatus(null);
        assertNotNull(status);
        assertEquals(0, status.getTotalTasks());
    }

    // Helper methods

    private PlanningTask createTestTask(String description, TaskPriority priority, PlanningTaskStatus status) {
        PlanningTask task = new PlanningTask();
        task.description = description;
        task.priority = priority;
        task.status = status;
        task.depth = 0;
        task.retryCount = 0;
        task.maxRetryCount = 3;
        return task;
    }
}