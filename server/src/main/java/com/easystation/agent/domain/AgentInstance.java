package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.infra.domain.Host;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_instance")
@Getter
@Setter
public class AgentInstance extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id", nullable = false)
    public Host host;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    public AgentTemplate template;

    @Enumerated(EnumType.STRING)
    public AgentStatus status = AgentStatus.UNCONFIGURED;

    public String version;

    public LocalDateTime lastHeartbeatTime;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
