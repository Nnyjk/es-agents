package com.easystation.deployment.domain;

import com.easystation.deployment.enums.PipelineStatus;
import com.easystation.deployment.enums.PipelineType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_pipeline")
@Getter
@Setter
public class DeploymentPipeline extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_type", nullable = false)
    public PipelineType pipelineType = PipelineType.BUILD_DEPLOY;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(columnDefinition = "TEXT")
    public String stages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PipelineStatus status = PipelineStatus.PENDING;

    @Column(name = "trigger_type")
    public String triggerType = "manual";

    @Column(name = "cron_expression")
    public String cronExpression;

    @Column(name = "last_execution_at")
    public LocalDateTime lastExecutionAt;

    @Column(name = "last_execution_status")
    public String lastExecutionStatus;

    @Column(name = "created_by")
    public String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}