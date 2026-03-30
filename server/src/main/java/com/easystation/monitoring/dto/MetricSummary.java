package com.easystation.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控指标摘要 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricSummary {
    /**
     * CPU 使用率 (%)
     */
    private double cpuUsage;

    /**
     * 内存使用率 (%)
     */
    private double memoryUsage;

    /**
     * 磁盘使用率 (%)
     */
    private double diskUsage;

    /**
     * Agent 数量
     */
    private long agentCount;

    /**
     * 任务数量
     */
    private long taskCount;

    /**
     * 告警数量
     */
    private long alertCount;
}