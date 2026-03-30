package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.AgentTaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "AgentTask")
@Table(name = "agent_task")
@Getter
@Setter
public class AgentTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "agent_instance_id", nullable = false)
    public AgentInstance agentInstance;

    @ManyToOne
    @JoinColumn(name = "command_id")
    public AgentCommand command;

    @Enumerated(EnumType.STRING)
    public AgentTaskStatus status = AgentTaskStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    public String args;

    @Column(columnDefinition = "TEXT")
    public String result;

    @Column(name = "exit_code")
    public Integer exitCode;

    @Column(name = "duration_ms")
    public Long durationMs;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
