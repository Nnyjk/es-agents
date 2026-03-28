package com.easystation.deployment.domain;

import com.easystation.deployment.enums.ProgressStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署进展实体
 */
@Entity
@Table(name = "deployment_progress", indexes = {
    @Index(name = "idx_progress_deployment", columnList = "deployment_id"),
    @Index(name = "idx_progress_status", columnList = "status"),
    @Index(name = "idx_progress_created", columnList = "created_at"),
    @Index(name = "idx_progress_deployment_status", columnList = "deployment_id, status")
})
@Getter
@Setter
public class DeploymentProgress extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "deployment_id", nullable = false)
    public UUID deploymentId;

    @Column(name = "stage", nullable = false, length = 50)
    public String stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public ProgressStatus status = ProgressStatus.PENDING;

    @Column(name = "progress_percent")
    public Integer progressPercent = 0;

    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * 根据部署ID查询进展列表
     */
    public static java.util.List<DeploymentProgress> findByDeploymentId(UUID deploymentId) {
        return find("deploymentId", deploymentId).list();
    }

    /**
     * 根据部署ID查询当前进展（最新的）
     */
    public static DeploymentProgress findCurrentByDeploymentId(UUID deploymentId) {
        return find("deploymentId", Sort.by("createdAt").descending(), deploymentId).firstResult();
    }

    /**
     * 根据部署ID和阶段查询进展
     */
    public static DeploymentProgress findByDeploymentIdAndStage(UUID deploymentId, String stage) {
        return find("deploymentId = ?1 AND stage = ?2", deploymentId, stage).firstResult();
    }
}