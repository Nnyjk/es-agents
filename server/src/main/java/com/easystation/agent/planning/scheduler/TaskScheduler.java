package com.easystation.agent.planning.scheduler;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.engine.TaskGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 任务调度器接口
 * 提供任务调度、优先级计算和队列管理能力
 */
public interface TaskScheduler {

    /**
     * 调度指定目标的所有任务
     * 将 READY 状态的任务转换为 SCHEDULED 状态
     *
     * @param goalId 目标 ID
     * @return 被调度的任务列表
     */
    List<PlanningTask> scheduleGoal(UUID goalId);

    /**
     * 调度单个任务
     *
     * @param task 要调度的任务
     * @return 调度后的任务
     */
    PlanningTask scheduleTask(PlanningTask task);

    /**
     * 获取下一个可执行的任务
     * 优先级最高且依赖已满足的任务
     *
     * @param goalId 目标 ID
     * @return 下一个可执行的任务，如果没有则返回空
     */
    Optional<PlanningTask> getNextExecutableTask(UUID goalId);

    /**
     * 获取下一个可执行的任务（全局）
     *
     * @return 下一个可执行的任务
     */
    Optional<PlanningTask> getNextExecutableTask();

    /**
     * 获取指定目标的所有可执行任务（按优先级排序）
     *
     * @param goalId 目标 ID
     * @return 可执行任务列表
     */
    List<PlanningTask> getExecutableTasks(UUID goalId);

    /**
     * 检查任务是否可以执行（所有依赖已完成）
     *
     * @param task 任务
     * @return 是否可执行
     */
    boolean canExecute(PlanningTask task);

    /**
     * 检查任务的所有依赖是否已完成
     *
     * @param taskId 任务 ID
     * @return 是否所有依赖已完成
     */
    boolean areDependenciesMet(UUID taskId);

    /**
     * 计算任务的调度优先级分数
     * 考虑基础优先级、深度、依赖数量等因素
     *
     * @param task 任务
     * @return 优先级分数
     */
    int calculatePriorityScore(PlanningTask task);

    /**
     * 更新任务队列（重新排序）
     *
     * @param goalId 目标 ID
     */
    void refreshQueue(UUID goalId);

    /**
     * 获取任务调度状态
     *
     * @param goalId 目标 ID
     * @return 调度状态
     */
    ScheduleStatus getScheduleStatus(UUID goalId);

    /**
     * 取消任务的调度
     *
     * @param taskId 任务 ID
     * @return 是否成功取消
     */
    boolean cancelSchedule(UUID taskId);

    /**
     * 重新调度失败的任务
     *
     * @param taskId 任务 ID
     * @return 是否成功重新调度
     */
    boolean rescheduleFailedTask(UUID taskId);

    /**
     * 获取任务执行顺序（基于依赖关系和优先级）
     *
     * @param goalId 目标 ID
     * @return 排序后的任务列表
     */
    List<PlanningTask> getExecutionOrder(UUID goalId);

    /**
     * 构建任务图并获取调度信息
     *
     * @param goalId 目标 ID
     * @return 任务图
     */
    TaskGraph buildTaskGraph(UUID goalId);

    /**
     * 调度状态信息
     */
    class ScheduleStatus {
        private final UUID goalId;
        private final int totalTasks;
        private final int scheduledTasks;
        private final int runningTasks;
        private final int completedTasks;
        private final int failedTasks;
        private final int pendingTasks;
        private final boolean hasBlockingTasks;

        public ScheduleStatus(UUID goalId, int totalTasks, int scheduledTasks,
                              int runningTasks, int completedTasks, int failedTasks,
                              int pendingTasks, boolean hasBlockingTasks) {
            this.goalId = goalId;
            this.totalTasks = totalTasks;
            this.scheduledTasks = scheduledTasks;
            this.runningTasks = runningTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
            this.pendingTasks = pendingTasks;
            this.hasBlockingTasks = hasBlockingTasks;
        }

        public UUID getGoalId() {
            return goalId;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public int getScheduledTasks() {
            return scheduledTasks;
        }

        public int getRunningTasks() {
            return runningTasks;
        }

        public int getCompletedTasks() {
            return completedTasks;
        }

        public int getFailedTasks() {
            return failedTasks;
        }

        public int getPendingTasks() {
            return pendingTasks;
        }

        public boolean hasBlockingTasks() {
            return hasBlockingTasks;
        }

        /**
         * 计算完成百分比
         */
        public double getCompletionPercentage() {
            if (totalTasks == 0) return 0;
            return (completedTasks * 100.0) / totalTasks;
        }

        /**
         * 是否所有任务已完成
         */
        public boolean isComplete() {
            return totalTasks > 0 && completedTasks == totalTasks;
        }

        /**
         * 是否有任务正在执行
         */
        public boolean isRunning() {
            return runningTasks > 0;
        }

        @Override
        public String toString() {
            return "ScheduleStatus{" +
                    "goalId=" + goalId +
                    ", total=" + totalTasks +
                    ", scheduled=" + scheduledTasks +
                    ", running=" + runningTasks +
                    ", completed=" + completedTasks +
                    ", failed=" + failedTasks +
                    ", pending=" + pendingTasks +
                    ", blocking=" + hasBlockingTasks +
                    ", progress=" + String.format("%.1f%%", getCompletionPercentage()) +
                    '}';
        }
    }
}