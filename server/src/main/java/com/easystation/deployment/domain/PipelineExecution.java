package com.easystation.deployment.domain;

import com.easystation.deployment.enums.PipelineStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "DeploymentPipelineExecution")
@Table(name = "deployment_pipeline_execution")
@Getter
@Setter
public class PipelineExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "pipeline_id", nullable = false)
    public UUID pipelineId;

    @Column(name = "execution_number")
    public Integer executionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PipelineStatus status = PipelineStatus.PENDING;

    @Column(name = "triggered_by")
    public String triggeredBy;

    @Column(name = "trigger_type")
    public String triggerType;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;

    @Column(columnDefinition = "TEXT")
    public String logs;

    @Column(name = "stage_results", columnDefinition = "TEXT")
    public String stageResults;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;
}