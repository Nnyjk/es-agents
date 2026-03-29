package com.easystation.agent.planning.scheduler;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.engine.TaskGraph;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import com.easystation.agent.planning.scheduler.impl.TaskSchedulerImpl;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
     * 测试调度单个任务
     */
    @Test
    void testScheduleTask() {
        PlanningTask task = createTestTask("Test Task", TaskPriority.HIGH, PlanningTaskStatus.READY);
        task.id = UUID.randomUUID();

        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTask scheduled = taskScheduler.scheduleTask(task);

        assertNotNull(scheduled);
        assertEquals(PlanningTaskStatus.SCHEDULED, scheduled.status);
        assertNotNull(scheduled.priorityValue);
        assertTrue(scheduled.priorityValue > 0);
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
     * 测试调度目标的所有任务
     */
    @Test
    void testScheduleGoal() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Task 1", TaskPriority.HIGH, PlanningTaskStatus.READY);
        PlanningTask task2 = createTestTask("Task 2", TaskPriority.NORMAL, PlanningTaskStatus.READY);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task1.goalId = goalId;
        task2.goalId = goalId;

        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.READY))
                .thenReturn(List.of(task1, task2));
        when(taskRepository.findByGoalId(goalId)).thenReturn(List.of(task1, task2));
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        List<PlanningTask> scheduled = taskScheduler.scheduleGoal(goalId);

        assertNotNull(scheduled);
        assertEquals(2, scheduled.size());
        assertTrue(scheduled.stream().allMatch(t -> t.status == PlanningTaskStatus.SCHEDULED));
    }

    /**
     * 测试获取下一个可执行任务
     */
    @Test
    void testGetNextExecutableTask() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("High Priority", TaskPriority.URGENT, PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Normal Priority", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task1.goalId = goalId;
        task2.goalId = goalId;

        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.SCHEDULED))
                .thenReturn(List.of(task1, task2));
        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.RETRYING))
                .thenReturn(Collections.emptyList());
        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());
        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        Optional<PlanningTask> nextTask = taskScheduler.getNextExecutableTask(goalId);

        assertTrue(nextTask.isPresent());
        // 高优先级任务应该被选中
        assertEquals(TaskPriority.URGENT, nextTask.get().priority);
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
        dep.dependencyType = "SOFT"; // 软依赖

        when(dependencyRepository.findByTaskId(task.id)).thenReturn(List.of(dep));

        boolean met = taskScheduler.areDependenciesMet(task.id);

        assertTrue(met); // 软依赖不阻塞
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
     * 测试重试次数对优先级的影响
     */
    @Test
    void testRetryCountAffectsPriority() {
        PlanningTask noRetryTask = createTestTask("No Retry", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        noRetryTask.id = UUID.randomUUID();
        noRetryTask.retryCount = 0;

        PlanningTask retryTask = createTestTask("Retry", TaskPriority.NORMAL, PlanningTaskStatus.RETRYING);
        retryTask.id = UUID.randomUUID();
        retryTask.retryCount = 2;

        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        int noRetryScore = taskScheduler.calculatePriorityScore(noRetryTask);
        int retryScore = taskScheduler.calculatePriorityScore(retryTask);

        assertTrue(noRetryScore > retryScore);
    }

    /**
     * 测试取消调度
     */
    @Test
    void testCancelSchedule() {
        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();

        when(taskRepository.findById(task.id)).thenReturn(task);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        boolean cancelled = taskScheduler.cancelSchedule(task.id);

        assertTrue(cancelled);
        assertEquals(PlanningTaskStatus.CANCELLED, task.status);
    }

    /**
     * 测试取消已完成任务（不应成功）
     */
    @Test
    void testCancelCompletedTask() {
        PlanningTask task = createTestTask("Completed", TaskPriority.NORMAL, PlanningTaskStatus.COMPLETED);
        task.id = UUID.randomUUID();

        when(taskRepository.findById(task.id)).thenReturn(task);

        boolean cancelled = taskScheduler.cancelSchedule(task.id);

        assertFalse(cancelled);
    }

    /**
     * 测试重新调度失败任务
     */
    @Test
    void testRescheduleFailedTask() {
        PlanningTask task = createTestTask("Failed Task", TaskPriority.NORMAL, PlanningTaskStatus.FAILED);
        task.id = UUID.randomUUID();
        task.retryCount = 1;
        task.maxRetryCount = 3;
        task.errorMessage = "Previous error";

        when(taskRepository.findById(task.id)).thenReturn(task);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        boolean rescheduled = taskScheduler.rescheduleFailedTask(task.id);

        assertTrue(rescheduled);
        assertEquals(PlanningTaskStatus.RETRYING, task.status);
        assertEquals(2, task.retryCount);
        assertNull(task.errorMessage);
    }

    /**
     * 测试重新调度达到最大重试次数的任务（不应成功）
     */
    @Test
    void testRescheduleMaxRetryTask() {
        PlanningTask task = createTestTask("Max Retry", TaskPriority.NORMAL, PlanningTaskStatus.FAILED);
        task.id = UUID.randomUUID();
        task.retryCount = 3;
        task.maxRetryCount = 3;

        when(taskRepository.findById(task.id)).thenReturn(task);

        boolean rescheduled = taskScheduler.rescheduleFailedTask(task.id);

        assertFalse(rescheduled);
    }

    /**
     * 测试获取调度状态
     */
    @Test
    void testGetScheduleStatus() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Scheduled", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Running", TaskPriority.NORMAL, PlanningTaskStatus.RUNNING);
        PlanningTask task3 = createTestTask("Completed", TaskPriority.NORMAL, PlanningTaskStatus.COMPLETED);
        PlanningTask task4 = createTestTask("Failed", TaskPriority.NORMAL, PlanningTaskStatus.FAILED);
        PlanningTask task5 = createTestTask("Ready", TaskPriority.NORMAL, PlanningTaskStatus.READY);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();
        task4.id = UUID.randomUUID();
        task5.id = UUID.randomUUID();

        when(taskRepository.findByGoalId(goalId)).thenReturn(List.of(task1, task2, task3, task4, task5));
        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        TaskScheduler.ScheduleStatus status = taskScheduler.getScheduleStatus(goalId);

        assertNotNull(status);
        assertEquals(5, status.getTotalTasks());
        assertEquals(1, status.getScheduledTasks());
        assertEquals(1, status.getRunningTasks());
        assertEquals(1, status.getCompletedTasks());
        assertEquals(1, status.getFailedTasks());
        assertEquals(1, status.getPendingTasks());
        assertEquals(20.0, status.getCompletionPercentage());
    }

    /**
     * 测试构建任务图
     */
    @Test
    void testBuildTaskGraph() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Task 1", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Task 2", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task1.goalId = goalId;
        task2.goalId = goalId;

        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task2;
        dep.dependsOnTask = task1;

        when(taskRepository.findByGoalId(goalId)).thenReturn(List.of(task1, task2));
        when(dependencyRepository.findByTaskId(task1.id)).thenReturn(Collections.emptyList());
        when(dependencyRepository.findByTaskId(task2.id)).thenReturn(List.of(dep));

        TaskGraph graph = taskScheduler.buildTaskGraph(goalId);

        assertNotNull(graph);
        assertEquals(2, graph.getTasks().size());
        assertEquals(1, graph.getDependencies().size());
        assertFalse(graph.detectCycle());
    }

    /**
     * 测试获取可执行任务列表
     */
    @Test
    void testGetExecutableTasks() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Task 1", TaskPriority.URGENT, PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Task 2", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask task3 = createTestTask("Task 3", TaskPriority.LOW, PlanningTaskStatus.RETRYING);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();
        task1.goalId = goalId;
        task2.goalId = goalId;
        task3.goalId = goalId;

        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.SCHEDULED))
                .thenReturn(List.of(task1, task2));
        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.RETRYING))
                .thenReturn(List.of(task3));
        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        List<PlanningTask> executable = taskScheduler.getExecutableTasks(goalId);

        assertNotNull(executable);
        assertEquals(3, executable.size());
        // 应按优先级排序
        assertEquals(TaskPriority.URGENT, executable.get(0).priority);
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

        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        assertTrue(taskScheduler.canExecute(scheduledTask));
        assertFalse(taskScheduler.canExecute(runningTask));
    }

    /**
     * 测试刷新队列
     */
    @Test
    void testRefreshQueue() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task = createTestTask("Task", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();
        task.goalId = goalId;

        when(taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.SCHEDULED))
                .thenReturn(List.of(task));
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        taskScheduler.refreshQueue(goalId);

        // 验证优先级值已更新
        assertNotNull(task.priorityValue);
    }

    /**
     * 测试获取执行顺序
     */
    @Test
    void testGetExecutionOrder() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Task 1", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Task 2", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);
        PlanningTask task3 = createTestTask("Task 3", TaskPriority.NORMAL, PlanningTaskStatus.SCHEDULED);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();
        task1.goalId = goalId;
        task2.goalId = goalId;
        task3.goalId = goalId;

        // task2 depends on task1
        // task3 depends on task2
        PlanningTaskDependency dep1 = new PlanningTaskDependency();
        dep1.task = task2;
        dep1.dependsOnTask = task1;

        PlanningTaskDependency dep2 = new PlanningTaskDependency();
        dep2.task = task3;
        dep2.dependsOnTask = task2;

        when(taskRepository.findByGoalId(goalId)).thenReturn(List.of(task1, task2, task3));
        when(dependencyRepository.findByTaskId(task1.id)).thenReturn(Collections.emptyList());
        when(dependencyRepository.findByTaskId(task2.id)).thenReturn(List.of(dep1));
        when(dependencyRepository.findByTaskId(task3.id)).thenReturn(List.of(dep2));
        when(dependencyRepository.countByDependsOnTaskId(any(UUID.class))).thenReturn(0L);

        List<PlanningTask> order = taskScheduler.getExecutionOrder(goalId);

        assertNotNull(order);
        assertEquals(3, order.size());
        // task1 应在 task2 之前
        int idx1 = order.indexOf(task1);
        int idx2 = order.indexOf(task2);
        int idx3 = order.indexOf(task3);

        assertTrue(idx1 < idx2);
        assertTrue(idx2 < idx3);
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