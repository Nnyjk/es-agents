package com.easystation.agent.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agent 性能指标记录
 */
@Entity
@Table(name = "agent_monitoring_metric", indexes = {
    @Index(name = "idx_agent_monitoring_metric_agent", columnList = "agent_instance_id"),
    @Index(name = "idx_agent_monitoring_metric_time", columnList = "collected_at")
})
@Getter
@Setter
public class AgentMetric extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "agent_instance_id", nullable = false)
    public AgentInstance agentInstance;

    /**
     * CPU 使用率 (0-100)
     */
    @Column(precision = 5, scale = 2)
    public Double cpuUsage;

    /**
     * 内存使用率 (0-100)
     */
    @Column(precision = 5, scale = 2)
    public Double memoryUsage;

    /**
     * 已使用内存 (MB)
     */
    public Long memoryUsedMb;

    /**
     * 总内存 (MB)
     */
    public Long memoryTotalMb;

    /**
     * 磁盘使用率 (0-100)
     */
    @Column(precision = 5, scale = 2)
    public Double diskUsage;

    /**
     * 已使用磁盘 (GB)
     */
    public Long diskUsedGb;

    /**
     * 总磁盘 (GB)
     */
    public Long diskTotalGb;

    /**
     * 网络入流量 (bytes/s)
     */
    public Long networkInBytes;

    /**
     * 网络出流量 (bytes/s)
     */
    public Long networkOutBytes;

    /**
     * 进程数
     */
    public Integer processCount;

    /**
     * 连接数
     */
    public Integer connectionCount;

    /**
     * 系统负载 (1分钟)
     */
    @Column(precision = 8, scale = 2)
    public Double loadAverage1;

    /**
     * 系统负载 (5分钟)
     */
    @Column(precision = 8, scale = 2)
    public Double loadAverage5;

    /**
     * 系统负载 (15分钟)
     */
    @Column(precision = 8, scale = 2)
    public Double loadAverage15;

    /**
     * 采集时间
     */
    @Column(nullable = false)
    public LocalDateTime collectedAt;

    @CreationTimestamp
    public LocalDateTime createdAt;
}