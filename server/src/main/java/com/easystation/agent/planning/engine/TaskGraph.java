package com.easystation.agent.planning.engine;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务图数据结构
 * 用于表示任务及其依赖关系的图结构
 * 支持拓扑排序和循环依赖检测
 */
public class TaskGraph {

    /** 所有任务节点 */
    private final List<PlanningTask> tasks;

    /** 所有依赖边 */
    private final List<PlanningTaskDependency> dependencies;

    /** 任务 ID 到任务的映射 */
    private final Map<UUID, PlanningTask> taskMap;

    /** 依赖关系图：key 为任务 ID，value 为该任务依赖的任务 ID 列表 */
    private final Map<UUID, List<UUID>> dependencyGraph;

    /** 反向依赖图：key 为任务 ID，value 为依赖该任务的任务 ID 列表 */
    private final Map<UUID, List<UUID>> reverseDependencyGraph;

    public TaskGraph(List<PlanningTask> tasks, List<PlanningTaskDependency> dependencies) {
        this.tasks = new ArrayList<>(tasks);
        this.dependencies = new ArrayList<>(dependencies);
        this.taskMap = new HashMap<>();
        this.dependencyGraph = new HashMap<>();
        this.reverseDependencyGraph = new HashMap<>();

        buildMaps();
        buildDependencyGraphs();
    }

    /**
     * 构建任务映射
     */
    private void buildMaps() {
        for (PlanningTask task : tasks) {
            taskMap.put(task.id, task);
        }
    }

    /**
     * 构建依赖关系图
     */
    private void buildDependencyGraphs() {
        for (PlanningTask task : tasks) {
            dependencyGraph.put(task.id, new ArrayList<>());
            reverseDependencyGraph.put(task.id, new ArrayList<>());
        }

        for (PlanningTaskDependency dep : dependencies) {
            if (dep.task != null && dep.dependsOnTask != null) {
                UUID taskId = dep.task.id;
                UUID dependsOnId = dep.dependsOnTask.id;

                dependencyGraph.computeIfAbsent(taskId, k -> new ArrayList<>()).add(dependsOnId);
                reverseDependencyGraph.computeIfAbsent(dependsOnId, k -> new ArrayList<>()).add(taskId);
            }
        }
    }

    /**
     * 获取所有任务
     */
    public List<PlanningTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * 获取所有依赖关系
     */
    public List<PlanningTaskDependency> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    /**
     * 根据任务 ID 获取任务
     */
    public PlanningTask getTask(UUID taskId) {
        return taskMap.get(taskId);
    }

    /**
     * 获取任务的依赖列表
     * @param taskId 任务 ID
     * @return 该任务依赖的任务 ID 列表
     */
    public List<UUID> getDependencies(UUID taskId) {
        return Collections.unmodifiableList(dependencyGraph.getOrDefault(taskId, Collections.emptyList()));
    }

    /**
     * 获取依赖该任务的任务列表
     * @param taskId 任务 ID
     * @return 依赖该任务的任务 ID 列表
     */
    public List<UUID> getDependents(UUID taskId) {
        return Collections.unmodifiableList(reverseDependencyGraph.getOrDefault(taskId, Collections.emptyList()));
    }

    /**
     * 获取任务的直接依赖数量
     */
    public int getDependencyCount(UUID taskId) {
        return dependencyGraph.getOrDefault(taskId, Collections.emptyList()).size();
    }

    /**
     * 获取依赖该任务的任务数量
     */
    public int getDependentCount(UUID taskId) {
        return reverseDependencyGraph.getOrDefault(taskId, Collections.emptyList()).size();
    }

    /**
     * 拓扑排序
     * 返回按依赖关系排序的任务列表，依赖任务在前，被依赖任务在后
     *
     * @return 排序后的任务列表，如果存在循环依赖则返回空列表
     */
    public List<PlanningTask> topologicalSort() {
        if (detectCycle()) {
            return Collections.emptyList();
        }

        // Kahn 算法实现拓扑排序
        Map<UUID, Integer> inDegree = new HashMap<>();
        Queue<UUID> queue = new LinkedList<>();
        List<PlanningTask> result = new ArrayList<>();

        // 计算入度
        for (PlanningTask task : tasks) {
            int degree = dependencyGraph.getOrDefault(task.id, Collections.emptyList()).size();
            inDegree.put(task.id, degree);
            if (degree == 0) {
                queue.offer(task.id);
            }
        }

        // 拓扑排序
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            PlanningTask task = taskMap.get(current);
            if (task != null) {
                result.add(task);
            }

            // 减少依赖该任务的任务的入度
            List<UUID> dependents = reverseDependencyGraph.getOrDefault(current, Collections.emptyList());
            for (UUID dependent : dependents) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    queue.offer(dependent);
                }
            }
        }

        return result;
    }

    /**
     * 反向拓扑排序
     * 返回按依赖关系排序的任务列表，被依赖任务在前，依赖任务在后
     * 这种排序适合用于规划执行顺序（先执行依赖任务）
     */
    public List<PlanningTask> reverseTopologicalSort() {
        List<PlanningTask> sorted = topologicalSort();
        Collections.reverse(sorted);
        return sorted;
    }

    /**
     * 检测是否存在循环依赖
     * 使用 DFS 深度优先搜索检测
     */
    public boolean detectCycle() {
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();

        for (PlanningTask task : tasks) {
            if (hasCycleDFS(task.id, visited, recursionStack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DFS 检测循环依赖
     */
    private boolean hasCycleDFS(UUID taskId, Set<UUID> visited, Set<UUID> recursionStack) {
        if (recursionStack.contains(taskId)) {
            return true;
        }

        if (visited.contains(taskId)) {
            return false;
        }

        visited.add(taskId);
        recursionStack.add(taskId);

        List<UUID> dependsOn = dependencyGraph.getOrDefault(taskId, Collections.emptyList());
        for (UUID dep : dependsOn) {
            if (hasCycleDFS(dep, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(taskId);
        return false;
    }

    /**
     * 查找循环依赖路径
     * 如果存在循环依赖，返回循环路径
     */
    public List<UUID> findCyclePath() {
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();
        Map<UUID, UUID> path = new HashMap<>();

        for (PlanningTask task : tasks) {
            List<UUID> cycle = findCycleDFS(task.id, visited, recursionStack, path);
            if (cycle != null) {
                return cycle;
            }
        }

        return Collections.emptyList();
    }

    /**
     * DFS 查找循环依赖路径
     */
    private List<UUID> findCycleDFS(UUID taskId, Set<UUID> visited, Set<UUID> recursionStack, Map<UUID, UUID> path) {
        if (recursionStack.contains(taskId)) {
            // 找到循环，构建路径
            List<UUID> cycle = new ArrayList<>();
            UUID current = taskId;
            cycle.add(current);
            while (path.containsKey(current) && path.get(current) != taskId) {
                current = path.get(current);
                cycle.add(current);
            }
            cycle.add(taskId);
            Collections.reverse(cycle);
            return cycle;
        }

        if (visited.contains(taskId)) {
            return null;
        }

        visited.add(taskId);
        recursionStack.add(taskId);

        List<UUID> dependsOn = dependencyGraph.getOrDefault(taskId, Collections.emptyList());
        for (UUID dep : dependsOn) {
            path.put(dep, taskId);
            List<UUID> cycle = findCycleDFS(dep, visited, recursionStack, path);
            if (cycle != null) {
                return cycle;
            }
        }

        recursionStack.remove(taskId);
        return null;
    }

    /**
     * 获取可立即执行的任务（无依赖的任务）
     */
    public List<PlanningTask> getExecutableTasks() {
        return tasks.stream()
                .filter(task -> getDependencyCount(task.id) == 0)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务的层级（基于依赖深度）
     */
    public int getTaskLevel(UUID taskId) {
        if (getDependencyCount(taskId) == 0) {
            return 0;
        }

        int maxLevel = 0;
        for (UUID dep : getDependencies(taskId)) {
            maxLevel = Math.max(maxLevel, getTaskLevel(dep) + 1);
        }

        return maxLevel;
    }

    /**
     * 按层级分组任务
     */
    public Map<Integer, List<PlanningTask>> groupByLevel() {
        Map<Integer, List<PlanningTask>> levels = new HashMap<>();

        for (PlanningTask task : tasks) {
            int level = getTaskLevel(task.id);
            levels.computeIfAbsent(level, k -> new ArrayList<>()).add(task);
        }

        return levels;
    }

    /**
     * 获取图统计信息
     */
    public GraphStatistics getStatistics() {
        return new GraphStatistics(
                tasks.size(),
                dependencies.size(),
                detectCycle(),
                getExecutableTasks().size()
        );
    }

    /**
     * 图统计信息
     */
    public static class GraphStatistics {
        private final int taskCount;
        private final int dependencyCount;
        private final boolean hasCycle;
        private final int executableTaskCount;

        public GraphStatistics(int taskCount, int dependencyCount, boolean hasCycle, int executableTaskCount) {
            this.taskCount = taskCount;
            this.dependencyCount = dependencyCount;
            this.hasCycle = hasCycle;
            this.executableTaskCount = executableTaskCount;
        }

        public int getTaskCount() {
            return taskCount;
        }

        public int getDependencyCount() {
            return dependencyCount;
        }

        public boolean hasCycle() {
            return hasCycle;
        }

        public int getExecutableTaskCount() {
            return executableTaskCount;
        }

        @Override
        public String toString() {
            return "GraphStatistics{" +
                    "taskCount=" + taskCount +
                    ", dependencyCount=" + dependencyCount +
                    ", hasCycle=" + hasCycle +
                    ", executableTaskCount=" + executableTaskCount +
                    '}';
        }
    }
}