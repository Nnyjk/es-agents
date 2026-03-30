package com.easystation.agent.metrics.domain;

import com.easystation.agent.metrics.enums.MetricType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agent 指标数据
 */
@Entity(name = "AgentMetricRecord")
@Table(name = "agent_metric")
@Getter
@Setter
public class AgentMetric extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 关联的 Agent 实例 ID
     */
    @Column(nullable = false)
    public UUID agentId;

    /**
     * 关联的主机 ID
     */
    public UUID hostId;

    /**
     * 指标类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MetricType type;

    /**
     * 指标值
     */
    @Column(name = "metric_value", nullable = false)
    public Double value;

    /**
     * 指标标签（JSON 格式，用于附加信息）
     */
    @Column(columnDefinition = "TEXT")
    public String tags;

    /**
     * 采集时间
     */
    @Column(nullable = false)
    public LocalDateTime collectedAt;

    /**
     * 记录创建时间
     */
    @CreationTimestamp
    public LocalDateTime createdAt;
}
