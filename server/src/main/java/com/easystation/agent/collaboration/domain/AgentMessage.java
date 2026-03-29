package com.easystation.agent.collaboration.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Agent 消息实体
 */
@Entity
@Table(name = "agent_message")
public class AgentMessage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    public MessageType type;

    @Column(nullable = false, length = 255)
    public String fromAgentId;

    @Column(length = 255)
    public String toAgentId;

    @Column
    public Long correlationId;

    @Column(nullable = false, length = 255)
    public String subject;

    @Column(columnDefinition = "TEXT")
    public String content;

    @Column(columnDefinition = "TEXT")
    public String metadata;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
