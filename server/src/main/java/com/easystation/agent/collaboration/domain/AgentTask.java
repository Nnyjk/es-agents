package com.easystation.agent.collaboration.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 协作任务实体
 */
@Entity(name = "CollaborationTask")
@Table(name = "agent_collaboration_task")
public class AgentTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long sessionId;

    @Column(nullable = false, length = 255)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(length = 100)
    public String taskType;

    @Column(nullable = false, length = 50)
    public String priority = "medium";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    public TaskStatus status = TaskStatus.PENDING;

    @Column(length = 255)
    public String assignedTo;

    @Column(length = 255)
    public String createdBy;

    @Column(columnDefinition = "TEXT")
    public String result;

    @Column(columnDefinition = "TEXT")
    public String error;

    @Column
    public LocalDateTime assignedAt;

    @Column
    public LocalDateTime startedAt;

    @Column
    public LocalDateTime completedAt;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
