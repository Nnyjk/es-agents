package com.easystation.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统指标收集类
 * 收集并提供系统级 Prometheus 指标
 */
@ApplicationScoped
public class SystemMetrics {

    @Inject
    MeterRegistry meterRegistry;

    // 任务执行计数器
    private Counter taskExecutionsTotal;

    // 任务成功计数器
    private Counter taskSuccessTotal;

    // 任务失败计数器
    private Counter taskFailureTotal;

    // 在线 Agent 数量
    private final AtomicLong agentsOnline = new AtomicLong(0);

    // 活跃用户数
    private final AtomicLong activeUsers = new AtomicLong(0);

    // 系统指标
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    /**
     * 应用启动时初始化指标
     */
    void onStart(@Observes StartupEvent event) {
        init();
    }

    /**
     * 初始化指标
     */
    public void init() {
        // 任务执行总数计数器
        taskExecutionsTotal = Counter.builder("task_executions_total")
                .description("任务执行总数")
                .register(meterRegistry);

        // 任务成功计数器
        taskSuccessTotal = Counter.builder("task_success_total")
                .description("任务成功总数")
                .register(meterRegistry);

        // 任务失败计数器
        taskFailureTotal = Counter.builder("task_failure_total")
                .description("任务失败总数")
                .register(meterRegistry);

        // 在线 Agent 数量 Gauge
        Gauge.builder("esa_agents_online", agentsOnline, AtomicLong::get)
                .description("在线 Agent 数量")
                .register(meterRegistry);

        // 活跃用户数 Gauge
        Gauge.builder("esa_active_users", activeUsers, AtomicLong::get)
                .description("活跃用户数")
                .register(meterRegistry);

        // 系统 CPU 使用率 Gauge
        Gauge.builder("esa_system_cpu_usage", this, SystemMetrics::getCpuUsage)
                .description("系统 CPU 使用率百分比")
                .register(meterRegistry);

        // 系统内存使用率 Gauge
        Gauge.builder("esa_system_memory_usage", this, SystemMetrics::getMemoryUsage)
                .description("系统内存使用率百分比")
                .register(meterRegistry);

        // 系统磁盘使用率 Gauge
        Gauge.builder("esa_system_disk_usage", this, SystemMetrics::getDiskUsage)
                .description("系统磁盘使用率百分比")
                .register(meterRegistry);

        Log.info("系统指标初始化完成");
    }

    /**
     * 记录任务执行
     */
    public void recordTaskExecution() {
        if (taskExecutionsTotal != null) {
            taskExecutionsTotal.increment();
        }
    }

    /**
     * 记录任务成功
     */
    public void recordTaskSuccess() {
        if (taskSuccessTotal != null) {
            taskSuccessTotal.increment();
        }
        recordTaskExecution();
    }

    /**
     * 记录任务失败
     */
    public void recordTaskFailure() {
        if (taskFailureTotal != null) {
            taskFailureTotal.increment();
        }
        recordTaskExecution();
    }

    /**
     * 更新在线 Agent 数量
     */
    public void updateAgentsOnline(long count) {
        agentsOnline.set(count);
    }

    /**
     * 增加在线 Agent
     */
    public void incrementAgentsOnline() {
        agentsOnline.incrementAndGet();
    }

    /**
     * 减少在线 Agent
     */
    public void decrementAgentsOnline() {
        agentsOnline.decrementAndGet();
    }

    /**
     * 更新活跃用户数
     */
    public void updateActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * 获取当前在线 Agent 数量
     */
    public long getAgentsOnline() {
        return agentsOnline.get();
    }

    /**
     * 获取当前活跃用户数
     */
    public long getActiveUsers() {
        return activeUsers.get();
    }

    /**
     * 获取系统 CPU 使用率
     */
    public double getCpuUsage() {
        try {
            double load = osBean.getSystemLoadAverage();
            int processors = osBean.getAvailableProcessors();
            if (load >= 0 && processors > 0) {
                return Math.min((load / processors) * 100, 100);
            }
        } catch (Exception e) {
            Log.warnf("Failed to get CPU usage: %s", e.getMessage());
        }
        return 0.0;
    }

    /**
     * 获取系统内存使用率
     */
    public double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        if (maxMemory > 0) {
            return (usedMemory * 100.0) / maxMemory;
        }
        return 0.0;
    }

    /**
     * 获取系统磁盘使用率
     */
    public double getDiskUsage() {
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            if (totalSpace > 0) {
                return (usedSpace * 100.0) / totalSpace;
            }
        } catch (Exception e) {
            Log.warnf("Failed to get disk usage: %s", e.getMessage());
        }
        return 0.0;
    }
}