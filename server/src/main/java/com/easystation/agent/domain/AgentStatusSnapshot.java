package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.AgentStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agent 状态快照
 * 定期记录 Agent 状态用于历史分析
 */
@Entity
@Table(name = "agent_status_snapshot", indexes = {
    @Index(name = "idx_status_snapshot_agent", columnList = "agent_instance_id"),
    @Index(name = "idx_status_snapshot_time", columnList = "snapshot_time")
})
@Getter
@Setter
public class AgentStatusSnapshot extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "agent_instance_id", nullable = false)
    public AgentInstance agentInstance;

    /**
     * 快照时的状态
     */
    @Enumerated(EnumType.STRING)
    public AgentStatus status;

    /**
     * Agent 版本
     */
    public String version;

    /**
     * 心跳延迟（秒）
     */
    public Long heartbeatDelaySeconds;

    /**
     * 快照时间
     */
    @Column(name = "snapshot_time", nullable = false)
    public LocalDateTime snapshotTime;

    /**
     * 额外信息 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    public String extraInfo;

    @Column(name = "created_at")
    @CreationTimestamp
    public LocalDateTime createdAt;
}