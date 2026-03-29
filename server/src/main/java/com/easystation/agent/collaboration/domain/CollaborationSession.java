package com.easystation.agent.collaboration.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 协作会话实体
 */
@Entity
@Table(name = "agent_collaboration_session")
public class CollaborationSession extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 255)
    public String name;

    @Column(length = 1000)
    public String description;

    @Column(nullable = false, length = 50)
    public String status = "active";

    @Column(columnDefinition = "TEXT")
    public String agentIds;

    @Column(length = 255)
    public String creatorAgentId;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column
    public LocalDateTime updatedAt;

    @Column
    public LocalDateTime closedAt;

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
