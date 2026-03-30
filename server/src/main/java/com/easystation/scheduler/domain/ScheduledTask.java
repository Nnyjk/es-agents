package com.easystation.scheduler.domain;

import com.easystation.scheduler.enums.TaskStatus;
import com.easystation.scheduler.enums.TaskType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "SchedulerScheduledTask")
@Table(name = "scheduled_task")
@Getter
@Setter
public class ScheduledTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskType type;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false)
    public String cronExpression;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskStatus status;

    @Column(columnDefinition = "TEXT")
    public String config;

    @Column(name = "target_id")
    public UUID targetId;

    @Column(name = "target_type")
    public String targetType;

    @Column(name = "max_retries")
    public Integer maxRetries;

    @Column(name = "timeout_seconds")
    public Integer timeoutSeconds;

    @Column(name = "alert_on_failure")
    public boolean alertOnFailure;

    @Column(name = "last_execution_at")
    public LocalDateTime lastExecutionAt;

    @Column(name = "last_execution_status")
    public String lastExecutionStatus;

    @Column(name = "next_execution_at")
    public LocalDateTime nextExecutionAt;

    @Column(name = "created_by")
    public String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}