package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.AgentRepositoryType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_repository")
@Getter
@Setter
public class AgentRepository extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AgentRepositoryType type;

    @Column(name = "base_url", nullable = false)
    public String baseUrl;

    @Column(name = "project_path", nullable = false)
    public String projectPath;

    @Column(name = "default_branch")
    public String defaultBranch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credential_id")
    public AgentCredential credential;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
