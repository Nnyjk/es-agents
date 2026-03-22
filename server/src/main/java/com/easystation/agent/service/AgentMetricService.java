package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentMetric;
import com.easystation.agent.domain.AgentStatusSnapshot;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.agent.dto.AgentMetricRecord;
import com.easystation.agent.dto.AgentRuntimeStatus;
import com.easystation.alert.domain.AlertEvent;
import com.easystation.alert.domain.AlertRule;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agent 监控服务
 */
@ApplicationScoped
public class AgentMetricService {

    /**
     * 上报性能指标
     */
    @Transactional
    public void reportMetric(AgentMetricRecord.MetricReport report) {
        AgentInstance agent = AgentInstance.findById(report.agentId());
        if (agent == null) {
            throw new WebApplicationException("Agent not found", Response.Status.NOT_FOUND);
        }

        AgentMetric metric = new AgentMetric();
        metric.agentInstance = agent;
        metric.cpuUsage = report.cpuUsage();
        metric.memoryUsage = report.memoryUsage();
        metric.memoryUsedMb = report.memoryUsedMb();
        metric.memoryTotalMb = report.memoryTotalMb();
        metric.diskUsage = report.diskUsage();
        metric.diskUsedGb = report.diskUsedGb();
        metric.diskTotalGb = report.diskTotalGb();
        metric.networkInBytes = report.networkInBytes();
        metric.networkOutBytes = report.networkOutBytes();
        metric.processCount = report.processCount();
        metric.connectionCount = report.connectionCount();
        metric.loadAverage1 = report.loadAverage1();
        metric.loadAverage5 = report.loadAverage5();
        metric.loadAverage15 = report.loadAverage15();
        metric.collectedAt = report.collectedAt() != null ? report.collectedAt() : LocalDateTime.now();
        metric.persist();

        // 检查告警阈值
        checkMetricThresholds(agent, metric);

        Log.debugf("Reported metric for agent %s", report.agentId());
    }

    /**
     * 批量上报性能指标
     */
    @Transactional
    public void reportMetricsBatch(AgentMetricRecord.BatchMetricReport batch) {
        if (batch.metrics() == null || batch.metrics().isEmpty()) {
            return;
        }
        for (AgentMetricRecord.MetricReport report : batch.metrics()) {
            try {
                reportMetric(report);
            } catch (Exception e) {
                Log.warnf("Failed to report metric for agent %s: %s", report.agentId(), e.getMessage());
            }
        }
    }

    /**
     * 查询性能指标
     */
    public List<AgentMetricRecord.MetricDetail> queryMetrics(AgentMetricRecord.MetricQuery query) {
        StringBuilder jpql = new StringBuilder("from AgentMetric m where 1=1");
        Parameters params = new Parameters();

        if (query.agentId() != null) {
            jpql.append(" and m.agentInstance.id = :agentId");
            params.and("agentId", query.agentId());
        }
        if (query.startTime() != null) {
            jpql.append(" and m.collectedAt >= :startTime");
            params.and("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            jpql.append(" and m.collectedAt <= :endTime");
            params.and("endTime", query.endTime());
        }
        jpql.append(" order by m.collectedAt desc");

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        List<AgentMetric> metrics = AgentMetric.find(jpql.toString(), params)
                .range(offset, offset + limit - 1)
                .list();

        return metrics.stream()
                .map(this::toMetricDetail)
                .collect(Collectors.toList());
    }

    /**
     * 获取最新指标
     */
    public AgentMetricRecord.MetricDetail getLatestMetric(UUID agentId) {
        AgentMetric metric = AgentMetric.find("agentInstance.id = ?1 order by collectedAt desc", agentId)
                .firstResult();
        return metric != null ? toMetricDetail(metric) : null;
    }

    /**
     * 获取指标聚合数据
     */
    public AgentMetricRecord.MetricAggregation getMetricAggregation(UUID agentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AgentMetric> metrics = AgentMetric.find(
                "agentInstance.id = ?1 and collectedAt >= ?2 and collectedAt <= ?3 order by collectedAt",
                agentId, startTime, endTime
        ).list();

        if (metrics.isEmpty()) {
            return null;
        }

        AgentInstance agent = AgentInstance.findById(agentId);
        String agentName = agent != null && agent.template != null ? agent.template.name : "Unknown";

        DoubleSummaryStatistics cpuStats = metrics.stream()
                .filter(m -> m.cpuUsage != null)
                .mapToDouble(m -> m.cpuUsage)
                .summaryStatistics();
        DoubleSummaryStatistics memStats = metrics.stream()
                .filter(m -> m.memoryUsage != null)
                .mapToDouble(m -> m.memoryUsage)
                .summaryStatistics();
        LongSummaryStatistics netInStats = metrics.stream()
                .filter(m -> m.networkInBytes != null)
                .mapToLong(m -> m.networkInBytes)
                .summaryStatistics();
        LongSummaryStatistics netOutStats = metrics.stream()
                .filter(m -> m.networkOutBytes != null)
                .mapToLong(m -> m.networkOutBytes)
                .summaryStatistics();

        return new AgentMetricRecord.MetricAggregation(
                agentId,
                agentName,
                cpuStats.getCount() > 0 ? cpuStats.getAverage() : null,
                cpuStats.getCount() > 0 ? cpuStats.getMax() : null,
                cpuStats.getCount() > 0 ? cpuStats.getMin() : null,
                memStats.getCount() > 0 ? memStats.getAverage() : null,
                memStats.getCount() > 0 ? memStats.getMax() : null,
                memStats.getCount() > 0 ? memStats.getMin() : null,
                netInStats.getCount() > 0 ? (long) netInStats.getAverage() : null,
                netOutStats.getCount() > 0 ? (long) netOutStats.getAverage() : null,
                startTime,
                endTime,
                metrics.size()
        );
    }

    /**
     * 创建状态快照
     */
    @Transactional
    public void createStatusSnapshot(UUID agentId) {
        AgentInstance agent = AgentInstance.findById(agentId);
        if (agent == null) {
            return;
        }

        AgentStatusSnapshot snapshot = new AgentStatusSnapshot();
        snapshot.agentInstance = agent;
        snapshot.status = agent.status;
        snapshot.version = agent.version;
        snapshot.snapshotTime = LocalDateTime.now();

        if (agent.lastHeartbeatTime != null) {
            snapshot.heartbeatDelaySeconds = java.time.Duration.between(
                    agent.lastHeartbeatTime, LocalDateTime.now()
            ).getSeconds();
        }

        snapshot.persist();
        Log.debugf("Created status snapshot for agent %s", agentId);
    }

    /**
     * 查询状态快照
     */
    public List<AgentMetricRecord.SnapshotDetail> querySnapshots(AgentMetricRecord.SnapshotQuery query) {
        StringBuilder jpql = new StringBuilder("from AgentStatusSnapshot s where 1=1");
        Parameters params = new Parameters();

        if (query.agentId() != null) {
            jpql.append(" and s.agentInstance.id = :agentId");
            params.and("agentId", query.agentId());
        }
        if (query.startTime() != null) {
            jpql.append(" and s.snapshotTime >= :startTime");
            params.and("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            jpql.append(" and s.snapshotTime <= :endTime");
            params.and("endTime", query.endTime());
        }
        jpql.append(" order by s.snapshotTime desc");

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        List<AgentStatusSnapshot> snapshots = AgentStatusSnapshot.find(jpql.toString(), params)
                .range(offset, offset + limit - 1)
                .list();

        return snapshots.stream()
                .map(this::toSnapshotDetail)
                .collect(Collectors.toList());
    }

    /**
     * 获取监控统计数据
     */
    public AgentMetricRecord.MonitoringStats getMonitoringStats() {
        long totalAgents = AgentInstance.count();
        long onlineAgents = AgentInstance.count("status in ?1",
                List.of(AgentStatus.ONLINE, AgentStatus.DEPLOYED));
        long offlineAgents = AgentInstance.count("status = ?1", AgentStatus.OFFLINE);
        long errorAgents = AgentInstance.count("status = ?1", AgentStatus.ERROR);

        // 获取最新指标平均值
        String avgJpql = "select avg(m.cpuUsage), avg(m.memoryUsage), avg(m.diskUsage) " +
                "from AgentMetric m where m.id in " +
                "(select max(m2.id) from AgentMetric m2 group by m2.agentInstance.id)";

        Object[] avgs = (Object[]) AgentMetric.getEntityManager()
                .createQuery(avgJpql)
                .getSingleResult();

        Double avgCpu = avgs[0] != null ? ((Number) avgs[0]).doubleValue() : 0.0;
        Double avgMemory = avgs[1] != null ? ((Number) avgs[1]).doubleValue() : 0.0;
        Double avgDisk = avgs[2] != null ? ((Number) avgs[2]).doubleValue() : 0.0;

        // 获取 CPU 使用率 Top 5
        List<AgentMetricRecord.AgentMetricInfo> topCpuAgents = getTopAgentsByMetric("cpuUsage", 5);
        List<AgentMetricRecord.AgentMetricInfo> topMemoryAgents = getTopAgentsByMetric("memoryUsage", 5);

        return new AgentMetricRecord.MonitoringStats(
                (int) totalAgents,
                (int) onlineAgents,
                (int) offlineAgents,
                (int) errorAgents,
                avgCpu,
                avgMemory,
                avgDisk,
                topCpuAgents,
                topMemoryAgents
        );
    }

    /**
     * 批量获取 Agent 状态
     */
    public AgentMetricRecord.BatchStatusResult getBatchStatus(List<UUID> agentIds) {
        List<AgentInstance> agents;
        if (agentIds == null || agentIds.isEmpty()) {
            agents = AgentInstance.listAll();
        } else {
            agents = AgentInstance.find("id in ?1", agentIds).list();
        }

        List<AgentRuntimeStatus> statuses = new ArrayList<>();
        int online = 0, offline = 0, error = 0;

        for (AgentInstance agent : agents) {
            Long heartbeatAgeSeconds = null;
            if (agent.lastHeartbeatTime != null) {
                heartbeatAgeSeconds = java.time.Duration.between(
                        agent.lastHeartbeatTime, LocalDateTime.now()
                ).getSeconds();
            }

            boolean isOnline = agent.status == AgentStatus.ONLINE || agent.status == AgentStatus.DEPLOYED;
            if (isOnline) online++;
            else if (agent.status == AgentStatus.ERROR) error++;
            else offline++;

            String statusMessage = getStatusMessage(agent.status);

            statuses.add(new AgentRuntimeStatus(
                    agent.id,
                    agent.status,
                    agent.version,
                    agent.lastHeartbeatTime,
                    heartbeatAgeSeconds,
                    isOnline,
                    statusMessage,
                    agent.createdAt,
                    agent.updatedAt
            ));
        }

        return new AgentMetricRecord.BatchStatusResult(
                statuses, statuses.size(), online, offline, error
        );
    }

    /**
     * 触发监控告警
     */
    @Transactional
    public void triggerAlert(AgentMetricRecord.AlertTriggerRequest request) {
        AgentInstance agent = AgentInstance.findById(request.agentId());
        if (agent == null) {
            throw new WebApplicationException("Agent not found", Response.Status.NOT_FOUND);
        }

        AlertEvent event = new AlertEvent();
        event.eventType = AlertEventType.CUSTOM;
        event.level = determineAlertLevel(request.metricType(), request.currentValue(), request.threshold());
        event.status = AlertStatus.PENDING;
        event.title = String.format("Agent %s 监控告警", agent.template != null ? agent.template.name : request.agentId());
        event.message = request.message();
        event.resourceId = request.agentId();
        event.resourceType = "AGENT";
        event.persist();

        Log.infof("Triggered alert for agent %s: %s", request.agentId(), request.message());
    }

    /**
     * 检查指标阈值
     */
    private void checkMetricThresholds(AgentInstance agent, AgentMetric metric) {
        // 检查 CPU 使用率
        if (metric.cpuUsage != null && metric.cpuUsage > 90) {
            triggerAlert(new AgentMetricRecord.AlertTriggerRequest(
                    agent.id, "cpu", 90.0, metric.cpuUsage,
                    String.format("CPU 使用率过高: %.1f%%", metric.cpuUsage)
            ));
        }

        // 检查内存使用率
        if (metric.memoryUsage != null && metric.memoryUsage > 90) {
            triggerAlert(new AgentMetricRecord.AlertTriggerRequest(
                    agent.id, "memory", 90.0, metric.memoryUsage,
                    String.format("内存使用率过高: %.1f%%", metric.memoryUsage)
            ));
        }

        // 检查磁盘使用率
        if (metric.diskUsage != null && metric.diskUsage > 90) {
            triggerAlert(new AgentMetricRecord.AlertTriggerRequest(
                    agent.id, "disk", 90.0, metric.diskUsage,
                    String.format("磁盘使用率过高: %.1f%%", metric.diskUsage)
            ));
        }
    }

    /**
     * 确定告警级别
     */
    private AlertLevel determineAlertLevel(String metricType, double current, double threshold) {
        double ratio = current / threshold;
        if (ratio >= 1.5) return AlertLevel.CRITICAL;
        if (ratio >= 1.2) return AlertLevel.ERROR;
        return AlertLevel.WARNING;
    }

    /**
     * 获取指标 Top N Agent
     */
    private List<AgentMetricRecord.AgentMetricInfo> getTopAgentsByMetric(String metricField, int limit) {
        String jpql = String.format(
                "select m from AgentMetric m where m.id in " +
                        "(select max(m2.id) from AgentMetric m2 group by m2.agentInstance.id) " +
                        "order by m.%s desc", metricField
        );

        List<AgentMetric> metrics = AgentMetric.find(jpql).range(0, limit - 1).list();

        return metrics.stream()
                .map(m -> {
                    AgentInstance agent = m.agentInstance;
                    return new AgentMetricRecord.AgentMetricInfo(
                            agent.id,
                            agent.template != null ? agent.template.name : "Unknown",
                            agent.status != null ? agent.status.name() : "UNKNOWN",
                            m.cpuUsage,
                            m.memoryUsage,
                            m.diskUsage,
                            agent.lastHeartbeatTime
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取状态消息
     */
    private String getStatusMessage(AgentStatus status) {
        if (status == null) return "未知状态";
        return switch (status) {
            case UNCONFIGURED -> "未配置";
            case PREPARING -> "准备中";
            case READY -> "就绪";
            case PACKAGING -> "打包中";
            case PACKAGED -> "已打包";
            case DEPLOYING -> "部署中";
            case DEPLOYED -> "已部署";
            case ONLINE -> "在线";
            case OFFLINE -> "离线";
            case ERROR -> "错误";
        };
    }

    /**
     * 转换为 MetricDetail
     */
    private AgentMetricRecord.MetricDetail toMetricDetail(AgentMetric m) {
        AgentInstance agent = m.agentInstance;
        return new AgentMetricRecord.MetricDetail(
                m.id,
                agent.id,
                agent.template != null ? agent.template.name : "Unknown",
                m.cpuUsage,
                m.memoryUsage,
                m.memoryUsedMb,
                m.memoryTotalMb,
                m.diskUsage,
                m.diskUsedGb,
                m.diskTotalGb,
                m.networkInBytes,
                m.networkOutBytes,
                m.processCount,
                m.connectionCount,
                m.loadAverage1,
                m.loadAverage5,
                m.loadAverage15,
                m.collectedAt,
                m.createdAt
        );
    }

    /**
     * 转换为 SnapshotDetail
     */
    private AgentMetricRecord.SnapshotDetail toSnapshotDetail(AgentStatusSnapshot s) {
        AgentInstance agent = s.agentInstance;
        return new AgentMetricRecord.SnapshotDetail(
                s.id,
                agent.id,
                agent.template != null ? agent.template.name : "Unknown",
                s.status != null ? s.status.name() : "UNKNOWN",
                s.version,
                s.heartbeatDelaySeconds,
                s.extraInfo,
                s.snapshotTime,
                s.createdAt
        );
    }
}