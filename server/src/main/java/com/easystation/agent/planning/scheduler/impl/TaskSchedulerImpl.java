package com.easystation.agent.planning.scheduler.impl;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.engine.TaskGraph;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import com.easystation.agent.planning.scheduler.TaskScheduler;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务调度器实现
 * 提供优先级计算、依赖感知调度和任务队列管理
 */
@ApplicationScoped
public class TaskSchedulerImpl implements TaskScheduler {

    /** 优先级权重配置 */
    private static final int DEPTH_WEIGHT = 10;
    private static final int DEPENDENT_COUNT_WEIGHT = 5;
    private static final int ESTIMATED_TIME_WEIGHT = 2;

    @Inject
    PlanningTaskRepository taskRepository;

    @Inject
    PlanningTaskDependencyRepository dependencyRepository;

    @Override
    @Transactional
    public List<PlanningTask> scheduleGoal(UUID goalId) {
        if (goalId == null) {
            Log.warn("GoalId is null, cannot schedule");
            return Collections.emptyList();
        }

        Log.infof("Scheduling tasks for goal: %s", goalId);

        // 获取所有 READY 状态的任务
        List<PlanningTask> readyTasks = taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.READY);

        if (readyTasks.isEmpty()) {
            Log.infof("No READY tasks found for goal: %s", goalId);
            return Collections.emptyList();
        }

        // 构建任务图
        TaskGraph graph = buildTaskGraph(goalId);

        // 检查循环依赖
        if (graph.detectCycle()) {
            Log.errorf("Cycle detected in task graph for goal: %s", goalId);
            return Collections.emptyList();
        }

        // 按执行顺序调度
        List<PlanningTask> scheduledTasks = new ArrayList<>();
        for (PlanningTask task : readyTasks) {
            PlanningTask scheduled = scheduleTask(task);
            if (scheduled != null) {
                scheduledTasks.add(scheduled);
            }
        }

        Log.infof("Scheduled %d tasks for goal: %s", scheduledTasks.size(), goalId);
        return scheduledTasks;
    }

    @Override
    @Transactional
    public PlanningTask scheduleTask(PlanningTask task) {
        if (task == null || task.id == null) {
            Log.warn("Task is null or has no ID, cannot schedule");
            return null;
        }

        if (task.status != PlanningTaskStatus.READY && task.status != PlanningTaskStatus.CREATED) {
            Log.debugf("Task %s is not in READY/CREATED status, current: %s", task.id, task.status);
            return null;
        }

        // 计算优先级分数
        int priorityScore = calculatePriorityScore(task);
        task.priorityValue = priorityScore;

        // 更新状态为 SCHEDULED
        task.status = PlanningTaskStatus.SCHEDULED;
        taskRepository.persist(task);

        Log.debugf("Task %s scheduled with priority score: %d", task.id, priorityScore);
        return task;
    }

    @Override
    public Optional<PlanningTask> getNextExecutableTask(UUID goalId) {
        if (goalId == null) {
            return getNextExecutableTask();
        }

        // 获取所有可执行状态的任务
        List<PlanningTask> executableTasks = getExecutableTasks(goalId);

        if (executableTasks.isEmpty()) {
            return Optional.empty();
        }

        // 找到优先级最高且依赖满足的任务
        return executableTasks.stream()
                .filter(this::canExecute)
                .max(Comparator.comparingInt(this::calculatePriorityScore));
    }

    @Override
    public Optional<PlanningTask> getNextExecutableTask() {
        // 获取全局可执行任务
        List<PlanningTask> executableTasks = taskRepository.findExecutableTasks();

        if (executableTasks.isEmpty()) {
            return Optional.empty();
        }

        return executableTasks.stream()
                .filter(this::canExecute)
                .max(Comparator.comparingInt(this::calculatePriorityScore));
    }

    @Override
    public List<PlanningTask> getExecutableTasks(UUID goalId) {
        if (goalId == null) {
            return Collections.emptyList();
        }

        // 获取 SCHEDULED 和 RETRYING 状态的任务
        List<PlanningTask> scheduled = taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.SCHEDULED);
        List<PlanningTask> retrying = taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.RETRYING);

        List<PlanningTask> all = new ArrayList<>();
        all.addAll(scheduled);
        all.addAll(retrying);

        // 按优先级分数排序
        return all.stream()
                .sorted(Comparator.comparingInt(this::calculatePriorityScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(PlanningTask task) {
        if (task == null) {
            return false;
        }

        // 检查状态是否允许执行
        if (!task.canExecute()) {
            return false;
        }

        // 检查依赖是否满足
        return areDependenciesMet(task.id);
    }

    @Override
    public boolean areDependenciesMet(UUID taskId) {
        if (taskId == null) {
            return true;
        }

        // 获取任务的所有依赖
        List<PlanningTaskDependency> dependencies = dependencyRepository.findByTaskId(taskId);

        for (PlanningTaskDependency dep : dependencies) {
            PlanningTask dependsOnTask = dep.dependsOnTask;

            // 检查依赖任务是否已完成
            if (dependsOnTask != null) {
                if ("HARD".equals(dep.dependencyType)) {
                    // 硬依赖：必须完成
                    if (dependsOnTask.status != PlanningTaskStatus.COMPLETED) {
                        Log.debugf("Task %s blocked by hard dependency %s (status: %s)",
                                taskId, dependsOnTask.id, dependsOnTask.status);
                        return false;
                    }
                } else {
                    // 软依赖：期望完成，但未完成也可以继续
                    if (dependsOnTask.status == PlanningTaskStatus.FAILED
                            || dependsOnTask.status == PlanningTaskStatus.CANCELLED) {
                        Log.debugf("Task %s has failed soft dependency %s", taskId, dependsOnTask.id);
                        // 软依赖失败不阻塞，但记录日志
                    }
                }
            }
        }

        return true;
    }

    @Override
    public int calculatePriorityScore(PlanningTask task) {
        if (task == null) {
            return 0;
        }

        // 基础优先级
        int basePriority = task.priority.getValue();

        // 深度因素：深度越浅优先级越高
        int depthBonus = Math.max(0, DEPTH_WEIGHT - task.depth * 2);

        // 依赖数量因素：被依赖越多优先级越高（会阻塞更多任务）
        int dependentCount = (int) dependencyRepository.countByDependsOnTaskId(task.id);
        int dependentBonus = Math.min(dependentCount * DEPENDENT_COUNT_WEIGHT, 30);

        // 预估时间因素：预估时间越短优先级越高（快速完成任务）
        int timeBonus = 0;
        if (task.estimatedDurationSeconds != null) {
            // 预估时间越短，bonus 越高
            timeBonus = Math.max(0, ESTIMATED_TIME_WEIGHT - (int)(task.estimatedDurationSeconds / 60));
        }

        // 重试因素：重试次数越少优先级越高
        int retryPenalty = task.retryCount != null ? task.retryCount * 5 : 0;

        int totalScore = basePriority + depthBonus + dependentBonus + timeBonus - retryPenalty;

        Log.debugf("Priority score for task %s: base=%d, depth=%d, dep=%d, time=%d, retry=%d, total=%d",
                task.id, basePriority, depthBonus, dependentBonus, timeBonus, retryPenalty, totalScore);

        return Math.max(1, totalScore);
    }

    @Override
    @Transactional
    public void refreshQueue(UUID goalId) {
        if (goalId == null) {
            return;
        }

        Log.infof("Refreshing queue for goal: %s", goalId);

        // 重新计算所有 SCHEDULED 任务的优先级
        List<PlanningTask> scheduledTasks = taskRepository.findByGoalIdAndStatus(goalId, PlanningTaskStatus.SCHEDULED);

        for (PlanningTask task : scheduledTasks) {
            int newScore = calculatePriorityScore(task);
            task.priorityValue = newScore;
            taskRepository.persist(task);
        }

        Log.debugf("Refreshed %d tasks in queue", scheduledTasks.size());
    }

    @Override
    public ScheduleStatus getScheduleStatus(UUID goalId) {
        if (goalId == null) {
            return new ScheduleStatus(null, 0, 0, 0, 0, 0, 0, false);
        }

        List<PlanningTask> allTasks = taskRepository.findByGoalId(goalId);

        int total = allTasks.size();
        int scheduled = 0;
        int running = 0;
        int completed = 0;
        int failed = 0;
        int pending = 0;

        boolean hasBlocking = false;

        for (PlanningTask task : allTasks) {
            switch (task.status) {
                case SCHEDULED:
                    scheduled++;
                    if (!areDependenciesMet(task.id)) {
                        hasBlocking = true;
                    }
                    break;
                case RETRYING:
                    scheduled++; // 重试也算调度状态
                    break;
                case RUNNING:
                    running++;
                    break;
                case COMPLETED:
                    completed++;
                    break;
                case FAILED:
                    failed++;
                    break;
                case READY:
                case CREATED:
                    pending++;
                    break;
                default:
                    break;
            }
        }

        return new ScheduleStatus(goalId, total, scheduled, running, completed, failed, pending, hasBlocking);
    }

    @Override
    @Transactional
    public boolean cancelSchedule(UUID taskId) {
        if (taskId == null) {
            return false;
        }

        PlanningTask task = taskRepository.findById(taskId);
        if (task == null) {
            Log.warnf("Task %s not found for cancellation", taskId);
            return false;
        }

        if (!task.canCancel()) {
            Log.warnf("Task %s cannot be cancelled, status: %s", taskId, task.status);
            return false;
        }

        task.status = PlanningTaskStatus.CANCELLED;
        taskRepository.persist(task);

        Log.infof("Task %s cancelled", taskId);
        return true;
    }

    @Override
    @Transactional
    public boolean rescheduleFailedTask(UUID taskId) {
        if (taskId == null) {
            return false;
        }

        PlanningTask task = taskRepository.findById(taskId);
        if (task == null) {
            Log.warnf("Task %s not found for rescheduling", taskId);
            return false;
        }

        if (!task.canRetry()) {
            Log.warnf("Task %s cannot be retried, retryCount=%d, maxRetryCount=%d",
                    taskId, task.retryCount, task.maxRetryCount);
            return false;
        }

        // 更新状态为 RETRYING
        task.status = PlanningTaskStatus.RETRYING;
        task.retryCount++;
        task.errorMessage = null; // 清除之前的错误信息

        // 重新计算优先级
        task.priorityValue = calculatePriorityScore(task);

        taskRepository.persist(task);

        Log.infof("Task %s rescheduled for retry (attempt %d)", taskId, task.retryCount);
        return true;
    }

    @Override
    public List<PlanningTask> getExecutionOrder(UUID goalId) {
        if (goalId == null) {
            return Collections.emptyList();
        }

        TaskGraph graph = buildTaskGraph(goalId);

        if (graph.detectCycle()) {
            Log.warnf("Cycle detected in task graph for goal: %s", goalId);
            return Collections.emptyList();
        }

        // 使用反向拓扑排序（先执行依赖任务）
        List<PlanningTask> sortedTasks = graph.reverseTopologicalSort();

        // 进一步按优先级排序同层任务
        return sortedTasks.stream()
                .sorted((t1, t2) -> {
                    // 先按层级排序
                    int levelCompare = graph.getTaskLevel(t1.id) - graph.getTaskLevel(t2.id);
                    if (levelCompare != 0) {
                        return levelCompare;
                    }
                    // 同层级按优先级排序
                    return calculatePriorityScore(t2) - calculatePriorityScore(t1);
                })
                .collect(Collectors.toList());
    }

    @Override
    public TaskGraph buildTaskGraph(UUID goalId) {
        if (goalId == null) {
            return new TaskGraph(Collections.emptyList(), Collections.emptyList());
        }

        List<PlanningTask> tasks = taskRepository.findByGoalId(goalId);

        List<PlanningTaskDependency> dependencies = new ArrayList<>();
        for (PlanningTask task : tasks) {
            dependencies.addAll(dependencyRepository.findByTaskId(task.id));
        }

        return new TaskGraph(tasks, dependencies);
    }
}