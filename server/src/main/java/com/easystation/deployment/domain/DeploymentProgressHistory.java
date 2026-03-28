package com.easystation.deployment.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署进展历史实体
 */
@Entity
@Table(name = "deployment_progress_history", indexes = {
    @Index(name = "idx_progress_history_deployment", columnList = "deployment_id"),
    @Index(name = "idx_progress_history_created", columnList = "created_at")
})
@Getter
@Setter
public class DeploymentProgressHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "deployment_id", nullable = false)
    public UUID deploymentId;

    @Column(name = "stage", nullable = false, length = 50)
    public String stage;

    @Column(name = "old_status", length = 20)
    public String oldStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    public String newStatus;

    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * 根据部署ID查询状态变更历史
     */
    public static java.util.List<DeploymentProgressHistory> findByDeploymentId(UUID deploymentId) {
        return find("deploymentId", Sort.by("createdAt").descending(), deploymentId).list();
    }
}