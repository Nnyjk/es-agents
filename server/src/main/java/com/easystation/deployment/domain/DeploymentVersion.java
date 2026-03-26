package com.easystation.deployment.domain;

import com.easystation.deployment.enums.VersionStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署版本实体
 */
@Entity
@Table(name = "deployment_version", indexes = {
    @Index(name = "idx_version_app", columnList = "application_id"),
    @Index(name = "idx_version_env", columnList = "environment_id"),
    @Index(name = "idx_version_status", columnList = "status"),
    @Index(name = "idx_version_created", columnList = "created_at")
})
@Getter
@Setter
public class DeploymentVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "version_id", nullable = false, unique = true)
    public String versionId;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id", nullable = false)
    public UUID environmentId;

    @Column(name = "release_id")
    public UUID releaseId;

    @Column(nullable = false)
    public String version;

    @Column(name = "commit_hash")
    public String commitHash;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    public String commitMessage;

    @Column(name = "commit_author")
    public String commitAuthor;

    @Column(name = "commit_time")
    public LocalDateTime commitTime;

    @Column(name = "build_number")
    public Integer buildNumber;

    @Column(name = "build_url")
    public String buildUrl;

    @Column(name = "artifact_url")
    public String artifactUrl;

    @Column(name = "artifact_checksum")
    public String artifactChecksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public VersionStatus status = VersionStatus.DEPLOYING;

    @Column(columnDefinition = "TEXT")
    public String config;

    @Column(name = "deploy_config", columnDefinition = "TEXT")
    public String deployConfig;

    @Column(name = "deploy_by")
    public String deployBy;

    @Column(name = "deploy_at")
    public LocalDateTime deployAt;

    @Column(name = "deploy_duration")
    public Long deployDuration;

    @Column(name = "rollback_by")
    public String rollbackBy;

    @Column(name = "rollback_at")
    public LocalDateTime rollbackAt;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @Column(name = "is_stable")
    public Boolean isStable = false;

    @Column(name = "is_problematic")
    public Boolean isProblematic = false;

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