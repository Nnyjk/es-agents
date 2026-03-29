package com.easystation.agent.planning.engine;

import com.easystation.agent.planning.domain.PlanningTask;

import java.util.List;
import java.util.UUID;

/**
 * 任务分解引擎接口
 * 提供目标分解为可执行任务的能力
 */
public interface TaskDecompositionEngine {

    /**
     * 将目标分解为子任务列表
     *
     * @param goal 目标描述
     * @param maxDepth 最大分解深度
     * @return 分解后的任务列表（包含依赖关系）
     */
    List<PlanningTask> decompose(String goal, int maxDepth);

    /**
     * 将目标分解为子任务列表（使用默认最大深度）
     *
     * @param goal 目标描述
     * @return 分解后的任务列表
     */
    List<PlanningTask> decompose(String goal);

    /**
     * 构建任务依赖关系图
     *
     * @param tasks 任务列表
     * @return 任务图
     */
    TaskGraph buildDependencyGraph(List<PlanningTask> tasks);

    /**
     * 检测任务图中是否存在循环依赖
     *
     * @param graph 任务图
     * @return true 表示存在循环依赖
     */
    boolean detectCycle(TaskGraph graph);

    /**
     * 验证任务分解是否有效
     *
     * @param tasks 任务列表
     * @return 验证结果
     */
    DecompositionResult validate(List<PlanningTask> tasks);

    /**
     * 获取任务的执行顺序
     *
     * @param tasks 任务列表
     * @return 按执行顺序排列的任务列表
     */
    List<PlanningTask> getExecutionOrder(List<PlanningTask> tasks);

    /**
     * 创建根任务
     *
     * @param goal 目标描述
     * @return 创建的根任务
     */
    PlanningTask createRootTask(String goal);

    /**
     * 分解单个任务为子任务
     *
     * @param parentTask 父任务
     * @param goalId 目标 ID
     * @param depth 当前深度
     * @return 子任务列表
     */
    List<PlanningTask> decomposeTask(PlanningTask parentTask, UUID goalId, int depth);

    /**
     * 任务分解结果
     */
    class DecompositionResult {
        private final boolean valid;
        private final String message;
        private final List<String> errors;
        private final List<UUID> cyclePath;

        public DecompositionResult(boolean valid, String message) {
            this(valid, message, List.of(), List.of());
        }

        public DecompositionResult(boolean valid, String message, List<String> errors, List<UUID> cyclePath) {
            this.valid = valid;
            this.message = message;
            this.errors = errors;
            this.cyclePath = cyclePath;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<UUID> getCyclePath() {
            return cyclePath;
        }

        public static DecompositionResult success(String message) {
            return new DecompositionResult(true, message);
        }

        public static DecompositionResult failure(String message, List<String> errors) {
            return new DecompositionResult(false, message, errors, List.of());
        }

        public static DecompositionResult cycleDetected(String message, List<UUID> cyclePath) {
            return new DecompositionResult(false, message, List.of("存在循环依赖"), cyclePath);
        }
    }
}