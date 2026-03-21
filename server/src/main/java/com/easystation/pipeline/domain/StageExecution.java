package com.easystation.pipeline.domain;

import com.easystation.pipeline.enums.ExecutionStatus;
import com.easystation.pipeline.enums.StageType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_execution")
@Getter
@Setter
public class StageExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public UUID executionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StageType type;

    @Column(nullable = false)
    public String name;

    public int orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExecutionStatus status = ExecutionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    public String config;

    @Column(columnDefinition = "TEXT")
    public String logs;

    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    public LocalDateTime startedAt;

    public LocalDateTime finishedAt;

    public Long duration;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}