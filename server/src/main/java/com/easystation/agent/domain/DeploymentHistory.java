package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.DeploymentStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_history")
@Getter
@Setter
public class DeploymentHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "agent_instance_id", nullable = false)
    public AgentInstance agentInstance;

    @Column(nullable = false)
    public String version;

    @Enumerated(EnumType.STRING)
    public DeploymentStatus status = DeploymentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    public String config;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;

    @Column(name = "rollback_from")
    public UUID rollbackFrom;

    @Column(name = "rollback_by")
    public String rollbackBy;

    @Column(name = "rollback_at")
    public LocalDateTime rollbackAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "created_by")
    public String createdBy;

    /**
     * Find all deployment history for a given agent instance, ordered by creation time descending.
     */
    public static java.util.List<DeploymentHistory> findByAgentInstanceId(UUID agentInstanceId) {
        return find("agentInstance.id = ?1 order by createdAt desc", agentInstanceId).list();
    }

    /**
     * Find the latest successful deployment for a given agent instance.
     */
    public static DeploymentHistory findLatestSuccessful(UUID agentInstanceId) {
        return find("agentInstance.id = ?1 and status = ?2 order by createdAt desc", 
                agentInstanceId, DeploymentStatus.SUCCESS).firstResult();
    }

    /**
     * Count deployments for a given agent instance.
     */
    public static long countByAgentInstanceId(UUID agentInstanceId) {
        return count("agentInstance.id = ?1", agentInstanceId);
    }
}