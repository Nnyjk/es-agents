package com.easystation.agent.planning.engine.impl;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.engine.TaskDecompositionEngine;
import com.easystation.agent.planning.engine.TaskGraph;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 任务分解引擎实现
 * 提供目标分解、依赖构建和循环检测的核心能力
 */
@ApplicationScoped
public class TaskDecompositionEngineImpl implements TaskDecompositionEngine {

    /** 默认最大分解深度 */
    private static final int DEFAULT_MAX_DEPTH = 3;

    /** 任务分解模板 */
    private static final Pattern TASK_PATTERN = Pattern.compile(
            "(?m)^\\s*[-*]\\s*(.+)$|^\\s*\\d+[.)]\\s*(.+)$"
    );

    /** 依赖指示符模式 */
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
            "(需要|依赖|先|前置|等待|depends on|requires|after)\\s*[:：]?\\s*(.+)"
    );

    @Inject
    PlanningTaskRepository taskRepository;

    @Inject
    PlanningTaskDependencyRepository dependencyRepository;

    @Override
    public List<PlanningTask> decompose(String goal) {
        return decompose(goal, DEFAULT_MAX_DEPTH);
    }

    @Override
    @Transactional
    public List<PlanningTask> decompose(String goal, int maxDepth) {
        if (goal == null || goal.isBlank()) {
            Log.warn("Goal is empty, cannot decompose");
            return Collections.emptyList();
        }

        Log.infof("Decomposing goal: %s (maxDepth=%d)", goal, maxDepth);

        // 创建根任务
        PlanningTask rootTask = createRootTask(goal);

        // 生成目标 ID
        UUID goalId = UUID.randomUUID();
        rootTask.goalId = goalId;
        rootTask.status = PlanningTaskStatus.DECOMPOSING;
        taskRepository.persist(rootTask);

        // 分解任务
        List<PlanningTask> allTasks = new ArrayList<>();
        allTasks.add(rootTask);

        // 递归分解
        decomposeRecursive(rootTask, goalId, 0, maxDepth, allTasks);

        // 构建依赖关系
        buildDependencies(allTasks);

        // 更根任务状态为 READY
        rootTask.status = PlanningTaskStatus.READY;
        taskRepository.persist(rootTask);

        Log.infof("Decomposition completed: %d tasks created", allTasks.size());
        return allTasks;
    }

    /**
     * 递归分解任务
     */
    private void decomposeRecursive(PlanningTask parentTask, UUID goalId, int currentDepth, int maxDepth,
                                     List<PlanningTask> allTasks) {
        if (currentDepth >= maxDepth) {
            Log.debugf("Reached max depth %d for task %s", maxDepth, parentTask.id);
            return;
        }

        // 解析子任务
        List<PlanningTask> subTasks = decomposeTask(parentTask, goalId, currentDepth + 1);

        if (subTasks.isEmpty()) {
            Log.debugf("No subtasks found for task %s", parentTask.id);
            return;
        }

        // 添加子任务到列表
        allTasks.addAll(subTasks);

        // 保存子任务
        for (PlanningTask subTask : subTasks) {
            taskRepository.persist(subTask);

            // 继续递归分解
            decomposeRecursive(subTask, goalId, currentDepth + 1, maxDepth, allTasks);
        }
    }

    @Override
    public TaskGraph buildDependencyGraph(List<PlanningTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new TaskGraph(Collections.emptyList(), Collections.emptyList());
        }

        // 获取所有依赖关系
        List<UUID> taskIds = tasks.stream()
                .map(t -> t.id)
                .collect(Collectors.toList());

        List<PlanningTaskDependency> dependencies = new ArrayList<>();
        for (UUID taskId : taskIds) {
            dependencies.addAll(dependencyRepository.findByTaskId(taskId));
        }

        return new TaskGraph(tasks, dependencies);
    }

    @Override
    public boolean detectCycle(TaskGraph graph) {
        return graph != null && graph.detectCycle();
    }

    @Override
    public DecompositionResult validate(List<PlanningTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return DecompositionResult.failure("任务列表为空", List.of("没有可验证的任务"));
        }

        List<String> errors = new ArrayList<>();

        // 验证任务描述
        for (PlanningTask task : tasks) {
            if (task.description == null || task.description.isBlank()) {
                errors.add("任务 " + task.id + " 缺少描述");
            }
        }

        // 构建依赖图并检测循环
        TaskGraph graph = buildDependencyGraph(tasks);
        if (graph.detectCycle()) {
            List<UUID> cyclePath = graph.findCyclePath();
            return DecompositionResult.cycleDetected("存在循环依赖", cyclePath);
        }

        // 验证是否所有任务都有有效的父任务（除了根任务）
        for (PlanningTask task : tasks) {
            if (task.parentTask == null && task.depth > 0) {
                errors.add("非根任务 " + task.id + " 缺少父任务引用");
            }
        }

        if (errors.isEmpty()) {
            return DecompositionResult.success("任务分解有效，共 " + tasks.size() + " 个任务");
        } else {
            return DecompositionResult.failure("任务分解存在问题", errors);
        }
    }

    @Override
    public List<PlanningTask> getExecutionOrder(List<PlanningTask> tasks) {
        TaskGraph graph = buildDependencyGraph(tasks);
        return graph.reverseTopologicalSort();
    }

    @Override
    public PlanningTask createRootTask(String goal) {
        PlanningTask rootTask = new PlanningTask();
        rootTask.description = goal;
        rootTask.status = PlanningTaskStatus.CREATED;
        rootTask.priority = TaskPriority.HIGH;
        rootTask.depth = 0;
        rootTask.executorType = "ROOT";
        return rootTask;
    }

    @Override
    public List<PlanningTask> decomposeTask(PlanningTask parentTask, UUID goalId, int depth) {
        if (parentTask == null || parentTask.description == null) {
            return Collections.emptyList();
        }

        // 解析父任务描述，提取子任务
        List<String> subTaskDescriptions = parseSubTasks(parentTask.description);

        if (subTaskDescriptions.isEmpty()) {
            // 如果无法解析，尝试智能分解
            subTaskDescriptions = intelligentDecompose(parentTask.description);
        }

        // 创建子任务
        List<PlanningTask> subTasks = new ArrayList<>();
        for (int i = 0; i < subTaskDescriptions.size(); i++) {
            String desc = subTaskDescriptions.get(i);

            PlanningTask subTask = new PlanningTask();
            subTask.goalId = goalId;
            subTask.description = desc.trim();
            subTask.parentTask = parentTask;
            subTask.depth = depth;
            subTask.status = PlanningTaskStatus.CREATED;

            // 根据顺序设置优先级（第一个任务优先级最高）
            subTask.priority = calculateSubTaskPriority(parentTask.priority, i, subTaskDescriptions.size());
            subTask.executorType = determineExecutorType(desc);

            // 预估执行时间
            subTask.estimatedDurationSeconds = estimateDuration(desc);

            subTasks.add(subTask);
        }

        return subTasks;
    }

    /**
     * 解析子任务描述
     */
    private List<String> parseSubTasks(String description) {
        List<String> tasks = new ArrayList<>();
        Matcher matcher = TASK_PATTERN.matcher(description);

        while (matcher.find()) {
            String task1 = matcher.group(1);
            String task2 = matcher.group(2);
            String task = task1 != null ? task1 : task2;
            if (task != null && !task.isBlank()) {
                tasks.add(task.trim());
            }
        }

        return tasks;
    }

    /**
     * 智能分解（基于关键词）
     */
    private List<String> intelligentDecompose(String description) {
        List<String> tasks = new ArrayList<>();

        // 基于分隔符分解
        String[] parts = description.split("[,，;；\\n]+");
        for (String part : parts) {
            if (part.trim().length() > 5) {
                tasks.add(part.trim());
            }
        }

        // 如果还是空，尝试基于动词分解
        if (tasks.isEmpty()) {
            String[] verbs = {"完成", "实现", "创建", "配置", "部署", "测试", "验证", "检查"};
            for (String verb : verbs) {
                if (description.contains(verb)) {
                    int idx = description.indexOf(verb);
                    String rest = description.substring(idx + verb.length());
                    if (rest.length() > 5) {
                        tasks.add(verb + rest.split("[,，.。;；]")[0].trim());
                    }
                }
            }
        }

        return tasks;
    }

    /**
     * 构建任务依赖关系
     */
    private void buildDependencies(List<PlanningTask> allTasks) {
        // 按深度分组
        Map<Integer, List<PlanningTask>> tasksByDepth = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.depth));

        // 构建兄弟任务间的顺序依赖
        for (Map.Entry<Integer, List<PlanningTask>> entry : tasksByDepth.entrySet()) {
            List<PlanningTask> sameLevelTasks = entry.getValue();

            // 同层任务：前一任务完成后才能开始下一任务
            for (int i = 1; i < sameLevelTasks.size(); i++) {
                PlanningTask prevTask = sameLevelTasks.get(i - 1);
                PlanningTask currentTask = sameLevelTasks.get(i);

                if (prevTask.parentTask == currentTask.parentTask) {
                    createDependency(currentTask, prevTask);
                }
            }
        }

        // 构建父子依赖
        for (PlanningTask task : allTasks) {
            if (task.parentTask != null) {
                // 子任务完成后，父任务才能完成
                createDependency(task.parentTask, task);
            }
        }

        // 解析描述中的依赖指示
        for (PlanningTask task : allTasks) {
            parseDescriptionDependencies(task, allTasks);
        }
    }

    /**
     * 创建依赖关系
     */
    private void createDependency(PlanningTask task, PlanningTask dependsOnTask) {
        if (task.id == null || dependsOnTask.id == null) {
            return;
        }

        // 检查是否已存在
        if (dependencyRepository.existsDependency(task.id, dependsOnTask.id)) {
            return;
        }

        PlanningTaskDependency dep = PlanningTaskDependency.create(task, dependsOnTask);
        dependencyRepository.persist(dep);

        Log.debugf("Created dependency: %s depends on %s", task.id, dependsOnTask.id);
    }

    /**
     * 解析描述中的依赖指示
     */
    private void parseDescriptionDependencies(PlanningTask task, List<PlanningTask> allTasks) {
        Matcher matcher = DEPENDENCY_PATTERN.matcher(task.description);
        if (matcher.find()) {
            String dependencyRef = matcher.group(2);

            // 尝试匹配其他任务
            for (PlanningTask other : allTasks) {
                if (!other.id.equals(task.id) &&
                        other.description.toLowerCase().contains(dependencyRef.toLowerCase())) {
                    createDependency(task, other);
                    break;
                }
            }
        }
    }

    /**
     * 计算子任务优先级
     */
    private TaskPriority calculateSubTaskPriority(TaskPriority parentPriority, int index, int total) {
        // 子任务继承父任务的优先级，但稍低
        int baseValue = parentPriority.getValue();

        // 按顺序调整
        int adjustment = (total - index) * 2;

        return TaskPriority.fromValue(baseValue - adjustment);
    }

    /**
     * 判断执行器类型
     */
    private String determineExecutorType(String description) {
        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("部署") || lowerDesc.contains("deploy")) {
            return "DEPLOYMENT";
        }
        if (lowerDesc.contains("测试") || lowerDesc.contains("test")) {
            return "TEST";
        }
        if (lowerDesc.contains("配置") || lowerDesc.contains("config")) {
            return "CONFIGURATION";
        }
        if (lowerDesc.contains("监控") || lowerDesc.contains("monitor")) {
            return "MONITORING";
        }
        if (lowerDesc.contains("命令") || lowerDesc.contains("command")) {
            return "COMMAND";
        }

        return "DEFAULT";
    }

    /**
     * 预估执行时间
     */
    private Long estimateDuration(String description) {
        // 简单的预估逻辑：基于描述长度和关键词
        int length = description.length();

        // 基础时间
        long baseSeconds = Math.max(30, length / 2);

        // 关键词调整
        if (description.contains("测试")) {
            baseSeconds += 120; // 测试通常需要更长时间
        }
        if (description.contains("部署")) {
            baseSeconds += 180; // 部署需要更长时间
        }
        if (description.contains("配置")) {
            baseSeconds += 60;
        }

        return Math.min(baseSeconds, 600); // 最大 10 分钟
    }
}