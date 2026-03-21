package com.easystation.agent.metrics.service;

import com.easystation.agent.metrics.domain.AgentMetric;
import com.easystation.agent.metrics.dto.MetricRecord;
import com.easystation.agent.metrics.enums.MetricType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentMetricService {

    @Inject
    ObjectMapper objectMapper;

    /**
     * 上报指标数据
     */
    @Transactional
    public void report(MetricRecord.Report dto) {
        for (MetricRecord.MetricData data : dto.metrics()) {
            AgentMetric metric = new AgentMetric();
            metric.agentId = dto.agentId();
            metric.hostId = dto.hostId();
            metric.type = data.type();
            metric.value = data.value();
            metric.tags = data.tags();
            metric.collectedAt = data.collectedAt() != null ? data.collectedAt() : LocalDateTime.now();
            metric.persist();
        }
        Log.infof("Reported %d metrics for agent %s", dto.metrics().size(), dto.agentId());
    }

    /**
     * 查询指标列表
     */
    public List<MetricRecord.Detail> list(MetricRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.agentId() != null) {
            sql.append(" and agentId = :agentId");
            params.put("agentId", query.agentId());
        }
        if (query.hostId() != null) {
            sql.append(" and hostId = :hostId");
            params.put("hostId", query.hostId());
        }
        if (query.types() != null && !query.types().isEmpty()) {
            sql.append(" and type in :types");
            params.put("types", query.types());
        }
        if (query.startTime() != null) {
            sql.append(" and collectedAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and collectedAt <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return AgentMetric.<AgentMetric>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    /**
     * 获取主机指标摘要
     */
    public MetricRecord.HostMetricsSummary getHostSummary(UUID hostId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);

        Map<MetricType, Double> latestMetrics = new EnumMap<>(MetricType.class);

        // 查询最近的主机指标
        List<MetricType> hostTypes = Arrays.asList(
                MetricType.HOST_CPU_USAGE,
                MetricType.HOST_MEMORY_USAGE,
                MetricType.HOST_DISK_USAGE,
                MetricType.HOST_NETWORK_IN,
                MetricType.HOST_NETWORK_OUT,
                MetricType.HOST_LOAD_1,
                MetricType.HOST_LOAD_5,
                MetricType.HOST_LOAD_15
        );

        for (MetricType type : hostTypes) {
            AgentMetric metric = AgentMetric.<AgentMetric>find(
                    "hostId = ?1 and type = ?2 and collectedAt >= ?3 order by collectedAt desc",
                    hostId, type, since)
                    .firstResult();
            if (metric != null) {
                latestMetrics.put(type, metric.value);
            }
        }

        if (latestMetrics.isEmpty()) {
            return null;
        }

        // 获取最后采集时间
        AgentMetric lastMetric = AgentMetric.<AgentMetric>find(
                "hostId = ?1 and collectedAt >= ?2 order by collectedAt desc",
                hostId, since)
                .firstResult();

        return new MetricRecord.HostMetricsSummary(
                hostId,
                null, // hostName 需要从 Host 服务获取
                latestMetrics.get(MetricType.HOST_CPU_USAGE),
                latestMetrics.get(MetricType.HOST_MEMORY_USAGE),
                latestMetrics.get(MetricType.HOST_DISK_USAGE),
                latestMetrics.get(MetricType.HOST_NETWORK_IN),
                latestMetrics.get(MetricType.HOST_NETWORK_OUT),
                latestMetrics.get(MetricType.HOST_LOAD_1),
                latestMetrics.get(MetricType.HOST_LOAD_5),
                latestMetrics.get(MetricType.HOST_LOAD_15),
                lastMetric != null ? lastMetric.collectedAt : null
        );
    }

    /**
     * 获取 Agent 进程指标摘要
     */
    public MetricRecord.AgentMetricsSummary getAgentSummary(UUID agentId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);

        Map<MetricType, Double> latestMetrics = new EnumMap<>(MetricType.class);

        // 查询最近的 Agent 指标
        List<MetricType> agentTypes = Arrays.asList(
                MetricType.AGENT_CPU_USAGE,
                MetricType.AGENT_MEMORY_USAGE,
                MetricType.AGENT_MEMORY_RSS,
                MetricType.AGENT_UPTIME,
                MetricType.AGENT_THREAD_COUNT,
                MetricType.AGENT_CONNECTION_COUNT,
                MetricType.AGENT_TASK_TOTAL,
                MetricType.AGENT_TASK_SUCCESS,
                MetricType.AGENT_TASK_FAILED
        );

        for (MetricType type : agentTypes) {
            AgentMetric metric = AgentMetric.<AgentMetric>find(
                    "agentId = ?1 and type = ?2 and collectedAt >= ?3 order by collectedAt desc",
                    agentId, type, since)
                    .firstResult();
            if (metric != null) {
                latestMetrics.put(type, metric.value);
            }
        }

        if (latestMetrics.isEmpty()) {
            return null;
        }

        // 获取最后采集时间
        AgentMetric lastMetric = AgentMetric.<AgentMetric>find(
                "agentId = ?1 and collectedAt >= ?2 order by collectedAt desc",
                agentId, since)
                .firstResult();

        return new MetricRecord.AgentMetricsSummary(
                agentId,
                null, // agentName 需要从 Agent 服务获取
                latestMetrics.get(MetricType.AGENT_CPU_USAGE),
                latestMetrics.get(MetricType.AGENT_MEMORY_USAGE),
                latestMetrics.get(MetricType.AGENT_MEMORY_RSS),
                latestMetrics.get(MetricType.AGENT_UPTIME) != null ? latestMetrics.get(MetricType.AGENT_UPTIME).longValue() : null,
                latestMetrics.get(MetricType.AGENT_THREAD_COUNT) != null ? latestMetrics.get(MetricType.AGENT_THREAD_COUNT).intValue() : null,
                latestMetrics.get(MetricType.AGENT_CONNECTION_COUNT) != null ? latestMetrics.get(MetricType.AGENT_CONNECTION_COUNT).intValue() : null,
                latestMetrics.get(MetricType.AGENT_TASK_TOTAL) != null ? latestMetrics.get(MetricType.AGENT_TASK_TOTAL).longValue() : null,
                latestMetrics.get(MetricType.AGENT_TASK_SUCCESS) != null ? latestMetrics.get(MetricType.AGENT_TASK_SUCCESS).longValue() : null,
                latestMetrics.get(MetricType.AGENT_TASK_FAILED) != null ? latestMetrics.get(MetricType.AGENT_TASK_FAILED).longValue() : null,
                lastMetric != null ? lastMetric.collectedAt : null
        );
    }

    /**
     * 获取指标历史趋势
     */
    public MetricRecord.MetricHistory getMetricHistory(UUID agentId, MetricType type,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        Integer intervalMinutes) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (intervalMinutes == null) {
            intervalMinutes = 5;
        }

        List<AgentMetric> metrics = AgentMetric.<AgentMetric>find(
                "agentId = ?1 and type = ?2 and collectedAt >= ?3 and collectedAt <= ?4 order by collectedAt asc",
                agentId, type, startTime, endTime)
                .list();

        if (metrics.isEmpty()) {
            return new MetricRecord.MetricHistory(type, type.getUnit(), type.getDescription(),
                    List.of(), null, null, null);
        }

        // 按时间间隔聚合
        List<MetricRecord.DataPoint> dataPoints = aggregateByInterval(metrics, intervalMinutes);

        // 计算统计值
        DoubleSummaryStatistics stats = metrics.stream()
                .mapToDouble(m -> m.value)
                .summaryStatistics();

        return new MetricRecord.MetricHistory(
                type,
                type.getUnit(),
                type.getDescription(),
                dataPoints,
                stats.getAverage(),
                stats.getMin(),
                stats.getMax()
        );
    }

    /**
     * 按时间间隔聚合数据点
     */
    private List<MetricRecord.DataPoint> aggregateByInterval(List<AgentMetric> metrics, int intervalMinutes) {
        Map<LocalDateTime, List<AgentMetric>> grouped = metrics.stream()
                .collect(Collectors.groupingBy(m ->
                        m.collectedAt.truncatedTo(ChronoUnit.MINUTES)
                                .minusMinutes(m.collectedAt.getMinute() % intervalMinutes)
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(m -> m.value)
                            .average()
                            .orElse(0.0);
                    return new MetricRecord.DataPoint(entry.getKey(), avg);
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除过期指标数据
     */
    @Transactional
    public long deleteExpired(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        long deleted = AgentMetric.delete("collectedAt < ?1", cutoff);
        Log.infof("Deleted %d expired metrics older than %d days", deleted, retentionDays);
        return deleted;
    }

    private MetricRecord.Detail toDetail(AgentMetric metric) {
        return new MetricRecord.Detail(
                metric.id,
                metric.agentId,
                metric.hostId,
                metric.type,
                metric.value,
                metric.tags,
                metric.collectedAt,
                metric.createdAt
        );
    }
}