package com.easystation.deployment.domain;

import com.easystation.deployment.enums.RollbackStatus;
import com.easystation.deployment.enums.RollbackStrategy;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署回滚记录实体
 */
@Entity
@Table(name = "deployment_rollback", indexes = {
    @Index(name = "idx_rollback_app", columnList = "application_id"),
    @Index(name = "idx_rollback_env", columnList = "environment_id"),
    @Index(name = "idx_rollback_status", columnList = "status"),
    @Index(name = "idx_rollback_created", columnList = "created_at")
})
@Getter
@Setter
public class DeploymentRollback extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "rollback_id", nullable = false, unique = true)
    public String rollbackId;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id", nullable = false)
    public UUID environmentId;

    @Column(name = "from_version_id", nullable = false)
    public UUID fromVersionId;

    @Column(name = "to_version_id", nullable = false)
    public UUID toVersionId;

    @Column(name = "from_version")
    public String fromVersion;

    @Column(name = "to_version")
    public String toVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RollbackStrategy strategy = RollbackStrategy.FAST;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RollbackStatus status = RollbackStatus.PENDING;

    @Column(name = "reason", columnDefinition = "TEXT")
    public String reason;

    @Column(name = "precheck_result", columnDefinition = "TEXT")
    public String precheckResult;

    @Column(name = "precheck_at")
    public LocalDateTime precheckAt;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "duration")
    public Long duration;

    @Column(columnDefinition = "TEXT")
    public String logs;

    @Column(name = "verify_result", columnDefinition = "TEXT")
    public String verifyResult;

    @Column(name = "verify_passed")
    public Boolean verifyPassed;

    @Column(name = "notify_config", columnDefinition = "TEXT")
    public String notifyConfig;

    @Column(name = "timeout_config")
    public Integer timeoutConfig = 300;

    @Column(name = "retry_count")
    public Integer retryCount = 0;

    @Column(name = "max_retry")
    public Integer maxRetry = 3;

    @Column(name = "triggered_by", nullable = false)
    public String triggeredBy;

    @Column(name = "triggered_at")
    public LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;
}