package com.easystation.agent.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Agent 监控数据记录 DTO
 */
public class AgentMetricRecord {

    /**
     * 性能指标上报请求
     */
    public record MetricReport(
        UUID agentId,
        Double cpuUsage,
        Double memoryUsage,
        Long memoryUsedMb,
        Long memoryTotalMb,
        Double diskUsage,
        Long diskUsedGb,
        Long diskTotalGb,
        Long networkInBytes,
        Long networkOutBytes,
        Integer processCount,
        Integer connectionCount,
        Double loadAverage1,
        Double loadAverage5,
        Double loadAverage15,
        LocalDateTime collectedAt
    ) {}

    /**
     * 状态上报请求
     */
    public record StatusReport(
        UUID agentId,
        String status,
        String version,
        LocalDateTime heartbeatTime,
        String extraInfo
    ) {}

    /**
     * 运行日志上报请求
     */
    public record LogReport(
        UUID agentId,
        String level,
        String source,
        String message,
        String stackTrace,
        LocalDateTime timestamp
    ) {}

    /**
     * 批量指标上报请求
     */
    public record BatchMetricReport(
        List<MetricReport> metrics
    ) {}

    /**
     * 性能指标查询参数
     */
    public record MetricQuery(
        UUID agentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer limit,
        Integer offset
    ) {}

    /**
     * 性能指标详情
     */
    public record MetricDetail(
        UUID id,
        UUID agentId,
        String agentName,
        Double cpuUsage,
        Double memoryUsage,
        Long memoryUsedMb,
        Long memoryTotalMb,
        Double diskUsage,
        Long diskUsedGb,
        Long diskTotalGb,
        Long networkInBytes,
        Long networkOutBytes,
        Integer processCount,
        Integer connectionCount,
        Double loadAverage1,
        Double loadAverage5,
        Double loadAverage15,
        LocalDateTime collectedAt,
        LocalDateTime createdAt
    ) {}

    /**
     * 指标聚合数据
     */
    public record MetricAggregation(
        UUID agentId,
        String agentName,
        Double avgCpuUsage,
        Double maxCpuUsage,
        Double minCpuUsage,
        Double avgMemoryUsage,
        Double maxMemoryUsage,
        Double minMemoryUsage,
        Long avgNetworkIn,
        Long avgNetworkOut,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer sampleCount
    ) {}

    /**
     * 状态快照查询参数
     */
    public record SnapshotQuery(
        UUID agentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer limit,
        Integer offset
    ) {}

    /**
     * 状态快照详情
     */
    public record SnapshotDetail(
        UUID id,
        UUID agentId,
        String agentName,
        String status,
        String version,
        Long heartbeatDelaySeconds,
        String extraInfo,
        LocalDateTime snapshotTime,
        LocalDateTime createdAt
    ) {}

    /**
     * 监控统计数据
     */
    public record MonitoringStats(
        Integer totalAgents,
        Integer onlineAgents,
        Integer offlineAgents,
        Integer errorAgents,
        Double avgCpuUsage,
        Double avgMemoryUsage,
        Double avgDiskUsage,
        List<AgentMetricInfo> topCpuAgents,
        List<AgentMetricInfo> topMemoryAgents
    ) {}

    /**
     * Agent 指标概览
     */
    public record AgentMetricInfo(
        UUID agentId,
        String agentName,
        String status,
        Double cpuUsage,
        Double memoryUsage,
        Double diskUsage,
        LocalDateTime lastHeartbeat
    ) {}

    /**
     * 监控告警触发请求
     */
    public record AlertTriggerRequest(
        UUID agentId,
        String metricType,
        Double threshold,
        Double currentValue,
        String message
    ) {}

    /**
     * 批量状态查询结果
     */
    public record BatchStatusResult(
        List<AgentRuntimeStatus> agents,
        Integer total,
        Integer online,
        Integer offline,
        Integer error
    ) {}
}