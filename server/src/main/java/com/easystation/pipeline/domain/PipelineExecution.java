package com.easystation.pipeline.domain;

import com.easystation.pipeline.enums.ExecutionStatus;
import com.easystation.pipeline.enums.TriggerType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "PipelineExecutionEntity")
@Table(name = "pipeline_execution")
@Getter
@Setter
public class PipelineExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public UUID pipelineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExecutionStatus status = ExecutionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TriggerType triggerType;

    public String triggeredBy;

    public UUID deploymentId;

    public String version;

    @Column(columnDefinition = "TEXT")
    public String logs;

    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    public LocalDateTime startedAt;

    public LocalDateTime finishedAt;

    public Long duration;

    public int currentStage = 0;

    public int totalStages = 0;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}