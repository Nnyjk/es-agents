package com.easystation.agent.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "agent_command")
@Getter
@Setter
public class AgentCommand extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String script;

    public Long timeout = 60L; // seconds

    @Column(columnDefinition = "TEXT")
    public String defaultArgs; // JSON or String

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    public AgentTemplate template;
}
