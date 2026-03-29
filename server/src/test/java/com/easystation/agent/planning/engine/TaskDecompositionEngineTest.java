package com.easystation.agent.planning.engine;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.engine.impl.TaskDecompositionEngineImpl;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        // Reset mocks
        Mockito.reset(taskRepository, dependencyRepository);
    }

    /**
     * 测试 TaskGraph 基本功能
     */
    @Test
    void testTaskGraphCreation() {
        // 创建测试任务
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        // 创建依赖关系
        PlanningTaskDependency dep1 = new PlanningTaskDependency();
        dep1.task = task2;
        dep1.dependsOnTask = task1;

        PlanningTaskDependency dep2 = new PlanningTaskDependency();
        dep2.task = task3;
        dep2.dependsOnTask = task1;

        List<PlanningTask> tasks = List.of(task1, task2, task3);
        List<PlanningTaskDependency> deps = List.of(dep1, dep2);

        TaskGraph graph = new TaskGraph(tasks, deps);

        // 验证基本属性
        assertEquals(3, graph.getTasks().size());
        assertEquals(2, graph.getDependencies().size());
        assertEquals(task1, graph.getTask(task1.id));

        // 验证依赖关系
        assertEquals(0, graph.getDependencyCount(task1.id));
        assertEquals(1, graph.getDependencyCount(task2.id));
        assertEquals(1, graph.getDependencyCount(task3.id));

        // 验证反向依赖
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
        PlanningTask task4 = createTestTask("Task 4", 2);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();
        task4.id = UUID.randomUUID();

        // task2 depends on task1
        // task3 depends on task2
        // task4 depends on task2
        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);
        PlanningTaskDependency dep3 = createDependency(task4, task2);

        List<PlanningTask> tasks = List.of(task1, task2, task3, task4);
        List<PlanningTaskDependency> deps = List.of(dep1, dep2, dep3);

        TaskGraph graph = new TaskGraph(tasks, deps);

        List<PlanningTask> sorted = graph.topologicalSort();

        assertEquals(4, sorted.size());

        // task1 应在最前面（无依赖）
        assertEquals(task1.id, sorted.get(0).id);

        // task2 应在 task1 之后
        int task1Index = sorted.indexOf(task1);
        int task2Index = sorted.indexOf(task2);
        assertTrue(task2Index > task1Index);

        // task3 和 task4 应在 task2 之后
        int task3Index = sorted.indexOf(task3);
        int task4Index = sorted.indexOf(task4);
        assertTrue(task3Index > task2Index);
        assertTrue(task4Index > task2Index);
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

        // task2 depends on task1
        // task3 depends on task2
        // task1 depends on task3 (creates cycle)
        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);
        PlanningTaskDependency dep3 = createDependency(task1, task3);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2, dep3));

        assertTrue(graph.detectCycle());

        // 验证循环路径
        List<UUID> cyclePath = graph.findCyclePath();
        assertFalse(cyclePath.isEmpty());
    }

    /**
     * 测试简单任务分解
     */
    @Test
    void testSimpleTaskDecomposition() {
        // Setup mocks
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            PlanningTask task = invocation.getArgument(0);
            if (task.id == null) {
                task.id = UUID.randomUUID();
            }
            return task;
        });

        when(dependencyRepository.persist(any(PlanningTaskDependency.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(dependencyRepository.existsDependency(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        String goal = "实现用户认证功能，包含以下步骤：\n" +
                "- 设计认证方案\n" +
                "- 实现登录接口\n" +
                "- 测试认证流程";

        List<PlanningTask> tasks = decompositionEngine.decompose(goal);

        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());

        // 应该有根任务 + 3个子任务
        assertTrue(tasks.size() >= 1);

        // 检查根任务
        PlanningTask rootTask = tasks.stream()
                .filter(t -> t.depth == 0)
                .findFirst()
                .orElse(null);

        assertNotNull(rootTask);
        assertEquals(goal, rootTask.description);
    }

    /**
     * 测试多层任务分解
     */
    @Test
    void testMultiLayerTaskDecomposition() {
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            PlanningTask task = invocation.getArgument(0);
            if (task.id == null) {
                task.id = UUID.randomUUID();
            }
            return task;
        });

        when(dependencyRepository.persist(any(PlanningTaskDependency.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        when(dependencyRepository.existsDependency(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        String goal = "部署应用系统\n" +
                "- 配置环境\n" +
                "- 部署服务\n" +
                "- 验证部署\n" +
                "  1. 检查服务状态\n" +
                "  2. 运行健康测试";

        List<PlanningTask> tasks = decompositionEngine.decompose(goal, 3);

        assertNotNull(tasks);

        // 检查层级分布
        Map<Integer, List<PlanningTask>> byDepth = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.depth));

        assertTrue(byDepth.containsKey(0)); // root
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
        task.description = null; // 缺少描述

        when(dependencyRepository.findByTaskId(any(UUID.class))).thenReturn(Collections.emptyList());

        TaskDecompositionEngine.DecompositionResult result = decompositionEngine.validate(List.of(task));

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
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
     * 测试获取执行顺序
     */
    @Test
    void testGetExecutionOrder() {
        PlanningTask task1 = createTestTask("Task 1", 0);
        PlanningTask task2 = createTestTask("Task 2", 1);
        PlanningTask task3 = createTestTask("Task 3", 1);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();

        task2.parentTask = task1;
        task3.parentTask = task1;

        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task1);

        when(dependencyRepository.findByTaskId(task1.id)).thenReturn(Collections.emptyList());
        when(dependencyRepository.findByTaskId(task2.id)).thenReturn(List.of(dep1));
        when(dependencyRepository.findByTaskId(task3.id)).thenReturn(List.of(dep2));

        List<PlanningTask> executionOrder = decompositionEngine.getExecutionOrder(List.of(task1, task2, task3));

        assertNotNull(executionOrder);

        // task1 应在 task2 和 task3 之前执行
        int task1Index = executionOrder.indexOf(task1);
        int task2Index = executionOrder.indexOf(task2);
        int task3Index = executionOrder.indexOf(task3);

        if (task1Index >= 0 && task2Index >= 0) {
            assertTrue(task1Index < task2Index);
        }
        if (task1Index >= 0 && task3Index >= 0) {
            assertTrue(task1Index < task3Index);
        }
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

        // task2 depends on task1, task3 depends on task2
        PlanningTaskDependency dep1 = createDependency(task2, task1);
        PlanningTaskDependency dep2 = createDependency(task3, task2);

        TaskGraph graph = new TaskGraph(List.of(task1, task2, task3), List.of(dep1, dep2));

        Map<Integer, List<PlanningTask>> levels = graph.groupByLevel();

        assertNotNull(levels);
        assertTrue(levels.containsKey(0));
        assertTrue(levels.containsKey(1));
        assertTrue(levels.containsKey(2));
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