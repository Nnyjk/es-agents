package com.easystation.pipeline.domain;

import com.easystation.pipeline.enums.PipelineStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pipeline")
@Getter
@Setter
public class Pipeline extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PipelineStatus status = PipelineStatus.DRAFT;

    public UUID environmentId;

    public UUID templateId;

    @Column(columnDefinition = "TEXT")
    public String stages;

    @Column(columnDefinition = "TEXT")
    public String triggerConfig;

    public boolean enabled = true;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}