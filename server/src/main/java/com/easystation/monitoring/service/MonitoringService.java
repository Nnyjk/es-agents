package com.easystation.monitoring.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentTask;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.alert.domain.AlertEvent;
import com.easystation.alert.enums.AlertStatus;
import com.easystation.monitoring.dto.MetricSummary;
import com.easystation.monitoring.dto.TimeSeriesData;
import com.easystation.monitoring.metrics.SystemMetrics;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控服务
 * 提供系统监控指标数据
 */
@ApplicationScoped
public class MonitoringService {

    @Inject
    SystemMetrics systemMetrics;

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取监控指标摘要
     */
    public MetricSummary getMetricSummary() {
        MetricSummary summary = new MetricSummary();

        // 系统指标
        summary.setCpuUsage(getSystemCpuUsage());
        summary.setMemoryUsage(getSystemMemoryUsage());
        summary.setDiskUsage(getSystemDiskUsage());

        // 业务指标
        summary.setAgentCount(getAgentCount());
        summary.setTaskCount(getTaskCount());
        summary.setAlertCount(getAlertCount());

        // 更新系统指标
        systemMetrics.updateAgentsOnline(summary.getAgentCount());

        Log.debugf("Metric summary: cpu=%.2f%%, memory=%.2f%%, disk=%.2f%%, agents=%d, tasks=%d, alerts=%d",
            summary.getCpuUsage(), summary.getMemoryUsage(), summary.getDiskUsage(),
            summary.getAgentCount(), summary.getTaskCount(), summary.getAlertCount());

        return summary;
    }

    /**
     * 获取时序数据
     */
    public List<TimeSeriesData> getTimeseriesData(String metric, String timeRange, String start, String end) {
        List<TimeSeriesData> data = new ArrayList<>();

        // 根据时间范围计算数据点数量
        int points = getTimeRangePoints(timeRange);

        // 生成模拟时序数据（实际应从 Prometheus 或数据库获取）
        LocalDateTime now = LocalDateTime.now();
        for (int i = points; i >= 0; i--) {
            LocalDateTime time = now.minusMinutes(i * getIntervalMinutes(timeRange));
            TimeSeriesData point = new TimeSeriesData();
            point.setTimestamp(time.format(formatter));
            point.setMetric(metric);
            point.setValue(generateMetricValue(metric, i));
            data.add(point);
        }

        return data;
    }

    /**
     * 获取指标趋势
     */
    public List<TimeSeriesData> getMetricTrend(String metric, String timeRange, String step) {
        return getTimeseriesData(metric, timeRange, null, null);
    }

    /**
     * 获取系统 CPU 使用率
     */
    private double getSystemCpuUsage() {
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
    private double getSystemMemoryUsage() {
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
    private double getSystemDiskUsage() {
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

    /**
     * 获取 Agent 数量（在线状态）
     */
    private long getAgentCount() {
        return AgentInstance.count("status", AgentStatus.ONLINE);
    }

    /**
     * 获取任务数量
     */
    private long getTaskCount() {
        return AgentTask.count();
    }

    /**
     * 获取告警数量（今日）
     */
    private long getAlertCount() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return AlertEvent.count("createdAt >= ?1 and status = ?2", todayStart, AlertStatus.PENDING);
    }

    /**
     * 根据时间范围获取数据点数量
     */
    private int getTimeRangePoints(String timeRange) {
        if (timeRange == null) return 60;
        switch (timeRange) {
            case "5m": return 5;
            case "15m": return 15;
            case "30m": return 30;
            case "1h": return 60;
            case "3h": return 90;
            case "6h": return 120;
            case "12h": return 144;
            case "24h": return 288;
            case "7d": return 168;
            case "30d": return 360;
            default: return 60;
        }
    }

    /**
     * 根据时间范围获取间隔分钟数
     */
    private int getIntervalMinutes(String timeRange) {
        if (timeRange == null) return 1;
        switch (timeRange) {
            case "5m": return 1;
            case "15m": return 1;
            case "30m": return 1;
            case "1h": return 1;
            case "3h": return 2;
            case "6h": return 5;
            case "12h": return 5;
            case "24h": return 5;
            case "7d": return 60;
            case "30d": return 120;
            default: return 1;
        }
    }

    /**
     * 生成模拟指标值
     */
    private double generateMetricValue(String metric, int offset) {
        // 使用当前系统值作为基准，添加随机波动
        double baseValue;
        switch (metric) {
            case "cpu_usage":
                baseValue = getSystemCpuUsage();
                break;
            case "memory_usage":
                baseValue = getSystemMemoryUsage();
                break;
            case "disk_usage":
                baseValue = getSystemDiskUsage();
                break;
            case "agent_count":
                baseValue = getAgentCount();
                break;
            case "task_count":
                baseValue = getTaskCount();
                break;
            case "alert_count":
                baseValue = getAlertCount();
                break;
            default:
                baseValue = 50.0;
        }

        // 添加一些随机波动（±10%）
        double fluctuation = (Math.random() - 0.5) * 0.2 * baseValue;
        return Math.max(0, Math.min(baseValue + fluctuation, 100));
    }
}