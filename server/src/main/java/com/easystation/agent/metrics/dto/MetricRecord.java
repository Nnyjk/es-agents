package com.easystation.agent.metrics.dto;

import com.easystation.agent.metrics.enums.MetricType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MetricRecord {

    /**
     * 指标上报请求
     */
    public record Report(
            @NotNull UUID agentId,
            UUID hostId,
            @NotNull List<MetricData> metrics
    ) {}

    /**
     * 单个指标数据
     */
    public record MetricData(
            @NotNull MetricType type,
            @NotNull Double value,
            String tags,
            LocalDateTime collectedAt
    ) {}

    /**
     * 指标查询请求
     */
    public record Query(
            UUID agentId,
            UUID hostId,
            List<MetricType> types,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}

    /**
     * 指标详情
     */
    public record Detail(
            UUID id,
            UUID agentId,
            UUID hostId,
            MetricType type,
            Double value,
            String tags,
            LocalDateTime collectedAt,
            LocalDateTime createdAt
    ) {}

    /**
     * 聚合指标数据
     */
    public record Aggregated(
            MetricType type,
            String unit,
            Double avg,
            Double min,
            Double max,
            Long count,
            List<DataPoint> dataPoints
    ) {}

    /**
     * 数据点
     */
    public record DataPoint(
            LocalDateTime time,
            Double value
    ) {}

    /**
     * 主机指标摘要
     */
    public record HostMetricsSummary(
            UUID hostId,
            String hostName,
            Double cpuUsage,
            Double memoryUsage,
            Double diskUsage,
            Double networkInRate,
            Double networkOutRate,
            Double load1,
            Double load5,
            Double load15,
            LocalDateTime collectedAt
    ) {}

    /**
     * Agent 进程指标摘要
     */
    public record AgentMetricsSummary(
            UUID agentId,
            String agentName,
            Double cpuUsage,
            Double memoryUsage,
            Double memoryRss,
            Long uptime,
            Integer threadCount,
            Integer connectionCount,
            Long taskTotal,
            Long taskSuccess,
            Long taskFailed,
            LocalDateTime collectedAt
    ) {}

    /**
     * 指标历史趋势
     */
    public record MetricHistory(
            MetricType type,
            String unit,
            String description,
            List<DataPoint> dataPoints,
            Double avgValue,
            Double minValue,
            Double maxValue
    ) {}
}