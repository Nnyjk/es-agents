package com.easystation.agent.planning.engine;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
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
 * 任务分解引擎单元测试
 */
@QuarkusTest
class TaskDecompositionEngineTest {

    @Inject
    TaskDecompositionEngine decompositionEngine;

    @InjectMock
    PlanningTaskRepository taskRepository;

    @InjectMock
    PlanningTaskDependencyRepository dependencyRepository;

    @BeforeEach
    void setup() {
        Mockito.reset(taskRepository, dependencyRepository);
    }

    /**
     * 测试 TaskGraph 基本功能
     */
    @Test
    void testTaskGraphCreation() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        PlanningTaskDependency dep1 = new PlanningTaskDependency();
        dep1.task = task2;
        dep1.dependsOnTask = task1;

        PlanningTaskDependency dep2 = new PlanningTaskDependency();
        dep2.task = task3;
        dep2.dependsOnTask = task1;

        List<PlanningTask> tasks = List.of(task1, task2, task3);
        List<PlanningTaskDependency> deps = List.of(dep1, dep2);

        TaskGraph graph = new TaskGraph(tasks, deps);

        assertEquals(3, graph.getTasks().size());
        assertEquals(2, graph.getDependencies().size());
        assertEquals(task1, graph.getTask(task1.id));

        assertEquals(0, graph.getDependencyCount(task1.id));
        assertEquals(1, graph.getDependencyCount(task2.id));
        assertEquals(1, graph.getDependencyCount(task3.id));

        assertEquals(2, graph.getDependentCount(task1.id));
        assertEquals(0, graph.getDependentCount(task2.id));
    }

    /**
     * 测试拓扑排序
     */
    @Test
    void testTopologicalSort() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 2);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2));

        List<PlanningTask> sorted = graph.topologicalSort();

        assertEquals(3, sorted.size());
        assertEquals(task1.id, sorted.get(0).id);

        int task1Index = sorted.indexOf(task1);
        int task2Index = sorted.indexOf(task2);
        int task3Index = sorted.indexOf(task3);

        assertTrue(task2Index > task1Index);
        assertTrue(task3Index > task2Index);
    }

    /**
     * 测试循环依赖检测 - 无循环
     */
    @Test
    void testDetectCycleNoCycle() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();

        PlanningTaskDependency dep = createDependency(task2, task1);

        TaskGraph graph = new TaskGraph(List.of(task1, task2), List.of(dep));

        assertFalse(graph.detectCycle());
    }

    /**
     * 测试循环依赖检测 - 存在循环
     */
    @Test
    void testDetectCycleWithCycle() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);
        PlanningTaskDependency dep3 = createDependency(task1, task3);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2, dep3));

        assertTrue(graph.detectCycle());

        List<UUID> cyclePath = graph.findCyclePath();
        assertFalse(cyclePath.isEmpty());
    }

    /**
     * 测试创建根任务
     */
    @Test
    void testCreateRootTask() {
        String goal = "测试目标";
        PlanningTask rootTask = decompositionEngine.createRootTask(goal);

        assertNotNull(rootTask);
        assertEquals(goal, rootTask.description);
        assertEquals(0, rootTask.depth);
        assertEquals(PlanningTaskStatus.CREATED, rootTask.status);
        assertEquals(TaskPriority.HIGH, rootTask.priority);
        assertEquals("ROOT", rootTask.executorType);
    }

    /**
     * 测试空目标分解
     */
    @Test
    void testDecomposeEmptyGoal() {
        List<PlanningTask> tasks = decompositionEngine.decompose("");
        assertTrue(tasks.isEmpty());

        tasks = decompositionEngine.decompose(null);
        assertTrue(tasks.isEmpty());
    }

    /**
     * 测试验证分解结果 - 有效
     */
    @Test
    void testValidateValidTasks() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        task2.parentTask = task1;

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();

        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        TaskDecompositionEngine.DecompositionResult result = decompositionEngine.validate(List.of(task1, task2));

        assertTrue(result.isValid());
    }

    /**
     * 测试验证分解结果 - 缺少描述
     */
    @Test
    void testValidateTaskWithoutDescription() {
        PlanningTask task = new PlanningTask();
        task.id = UUID.randomUUID();
        task.description = null;

        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        TaskDecompositionEngine.DecompositionResult result = decompositionEngine.validate(List.of(task));

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
    }

    /**
     * 测试 TaskGraph 统计信息
     */
    @Test
    void testTaskGraphStatistics() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();

        PlanningTaskDependency dep = createDependency(task2, task1);

        TaskGraph graph = new TaskGraph(List.of(task1, task2), List.of(dep));

        TaskGraph.GraphStatistics stats = graph.getStatistics();

        assertEquals(2, stats.getTaskCount());
        assertEquals(1, stats.getDependencyCount());
        assertFalse(stats.hasCycle());
        assertEquals(1, stats.getExecutableTaskCount());
    }

    /**
     * 测试按层级分组
     */
    @Test
    void testGroupByLevel() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 2);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2));

        Map<Integer, List<PlanningTask>> levels = graph.groupByLevel();

        assertNotNull(levels);
        assertTrue(levels.containsKey(0));
        assertTrue(levels.containsKey(1));
        assertTrue(levels.containsKey(2));
    }

    /**
     * 测试反向拓扑排序
     */
    @Test
    void testReverseTopologicalSort() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 2);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2));

        List<PlanningTask> sorted = graph.reverseTopologicalSort();

        assertEquals(3, sorted.size());

        // 反向拓扑排序：task3 应该在最前面
        int task1Index = sorted.indexOf(task1);
        int task2Index = sorted.indexOf(task2);
        int task3Index = sorted.indexOf(task3);

        assertTrue(task3Index < task2Index);
        assertTrue(task2Index < task1Index);
    }

    // Helper methods

    private PlanningTask createTestTask(String description, int depth) {
        PlanningTask task = new PlanningTask();
        task.description = description;
        task.depth = depth;
        task.status = PlanningTaskStatus.CREATED;
        task.priority = TaskPriority.NORMAL;
        return task;
    }

    private PlanningTaskDependency createDependency(PlanningTask task, PlanningTask dependsOnTask) {
        PlanningTaskDependency dep = new PlanningTaskDependency();
        dep.task = task;
        dep.dependsOnTask = dependsOnTask;
        dep.dependencyType = "HARD";
        return dep;
    }
}