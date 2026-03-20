package com.easystation.scheduler.domain;

import com.easystation.scheduler.enums.ExecutionStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_execution")
@Getter
@Setter
public class TaskExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "task_id", nullable = false)
    public UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExecutionStatus status;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "scheduled_at")
    public LocalDateTime scheduledAt;

    @Column(name = "trigger_type")
    public String triggerType;

    @Column(name = "triggered_by")
    public String triggeredBy;

    @Column(columnDefinition = "TEXT")
    public String result;

    @Column(columnDefinition = "TEXT")
    public String logs;

    @Column(name = "error_message")
    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "retry_count")
    public Integer retryCount;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}