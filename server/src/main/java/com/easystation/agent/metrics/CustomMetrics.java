package com.easystation.agent.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义业务指标
 * 跟踪 Agent 数量、任务执行等指标
 */
@ApplicationScoped
public class CustomMetrics {

    @Inject
    MeterRegistry registry;

    private AtomicInteger agentCount;
    private Counter taskExecutionCount;
    private Counter taskSuccessCount;
    private Counter taskFailureCount;
    private Timer taskExecutionTime;

    @PostConstruct
    public void init() {
        // Agent 数量指标
        agentCount = new AtomicInteger(0);
        Gauge.builder("esa.agent.count", agentCount, AtomicInteger::get)
                .description("当前 Agent 实例数量")
                .baseUnit("agents")
                .register(registry);

        // 任务执行指标
        taskExecutionCount = Counter.builder("esa.task.execution.total")
                .description("任务执行总次数")
                .baseUnit("tasks")
                .register(registry);

        taskSuccessCount = Counter.builder("esa.task.success.total")
                .description("成功任务数量")
                .baseUnit("tasks")
                .register(registry);

        taskFailureCount = Counter.builder("esa.task.failure.total")
                .description("失败任务数量")
                .baseUnit("tasks")
                .register(registry);

        // 任务执行时间
        taskExecutionTime = Timer.builder("esa.task.execution.time")
                .description("任务执行时间")
                .register(registry);
    }

    /**
     * 更新 Agent 数量
     */
    public void updateAgentCount(int count) {
        if (agentCount != null) {
            agentCount.set(count);
        }
    }

    /**
     * 记录任务执行
     */
    public void recordTaskExecution() {
        if (taskExecutionCount != null) {
            taskExecutionCount.increment();
        }
    }

    /**
     * 记录任务成功
     */
    public void recordTaskSuccess() {
        if (taskSuccessCount != null) {
            taskSuccessCount.increment();
        }
    }

    /**
     * 记录任务失败
     */
    public void recordTaskFailure() {
        if (taskFailureCount != null) {
            taskFailureCount.increment();
        }
    }

    /**
     * 记录任务执行时间
     */
    public Timer.Sample startTaskTimer() {
        return Timer.start(registry);
    }

    /**
     * 停止任务计时器
     */
    public void stopTaskTimer(Timer.Sample sample) {
        if (sample != null && taskExecutionTime != null) {
            sample.stop(taskExecutionTime);
        }
    }
}
