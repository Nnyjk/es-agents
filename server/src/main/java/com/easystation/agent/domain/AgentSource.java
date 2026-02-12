package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.AgentSourceType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_source")
@Getter
@Setter
public class AgentSource extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AgentSourceType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repository_id")
    public AgentRepository repository;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credential_id")
    public AgentCredential credential;

    @Column(columnDefinition = "TEXT")
    public String config; // JSON content

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
