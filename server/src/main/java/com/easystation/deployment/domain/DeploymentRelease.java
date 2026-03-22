package com.easystation.deployment.domain;

import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.enums.ReleaseType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_release")
@Getter
@Setter
public class DeploymentRelease extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "release_id", nullable = false, unique = true)
    public String releaseId;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id", nullable = false)
    public UUID environmentId;

    @Column(nullable = false)
    public String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReleaseType type = ReleaseType.PATCH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReleaseStatus status = ReleaseStatus.DRAFT;

    @Column(nullable = false)
    public String applicant;

    public String approver;

    @Column(name = "scheduled_at")
    public LocalDateTime scheduledAt;

    @Column(name = "deployed_at")
    public LocalDateTime deployedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "release_notes", columnDefinition = "TEXT")
    public String releaseNotes;

    @Column(columnDefinition = "TEXT")
    public String changes;

    @Column(name = "deploy_progress", columnDefinition = "TEXT")
    public String deployProgress;

    @Column(name = "rollback_from")
    public String rollbackFrom;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "deleted")
    public boolean deleted = false;
}