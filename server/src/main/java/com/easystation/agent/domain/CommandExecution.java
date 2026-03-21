package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.ExecutionStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "command_execution")
@Getter
@Setter
public class CommandExecution extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    public CommandTemplate template;

    @ManyToOne
    @JoinColumn(name = "agent_instance_id")
    public AgentInstance agentInstance;

    @Column(nullable = false)
    public String command; // Actual executed command

    @Column(columnDefinition = "TEXT")
    public String parameters; // JSON of actual parameters used

    @Enumerated(EnumType.STRING)
    public ExecutionStatus status = ExecutionStatus.PENDING;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "finished_at")
    public LocalDateTime finishedAt;

    @Column(columnDefinition = "TEXT")
    public String output; // Command output

    @Column(name = "exit_code")
    public Integer exitCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "retry_count")
    public Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "executed_by")
    public String executedBy;

    /**
     * Find executions by agent instance.
     */
    public static java.util.List<CommandExecution> findByAgentInstanceId(UUID agentInstanceId) {
        return find("agentInstance.id = ?1 order by createdAt desc", agentInstanceId).list();
    }

    /**
     * Find executions by template.
     */
    public static java.util.List<CommandExecution> findByTemplateId(UUID templateId) {
        return find("template.id = ?1 order by createdAt desc", templateId).list();
    }

    /**
     * Find recent executions.
     */
    public static java.util.List<CommandExecution> findRecent(int limit) {
        return find("order by createdAt desc").page(0, limit).list();
    }
}