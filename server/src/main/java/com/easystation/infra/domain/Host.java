package com.easystation.infra.domain;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.infra.domain.enums.HostStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "infra_host", uniqueConstraints = @UniqueConstraint(columnNames = "identifier"))
@Getter
@Setter
public class Host extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * Unique identifier for the host (user-defined or auto-generated)
     */
    @Column(nullable = false, unique = true)
    public String identifier;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String hostname; // IP or Domain

    public String os;
    public String cpuInfo;
    public String memInfo;

    /**
     * IP address of the host
     */
    public String ip;

    /**
     * Port number for host agent connection
     */
    @Column(columnDefinition = "integer default 9090")
    public Integer port = 9090;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "environment_id", nullable = false)
    public Environment environment;

    @Enumerated(EnumType.STRING)
    public HostStatus status = HostStatus.UNCONNECTED;

    /**
     * Secret key for Host Agent authentication (HMAC)
     */
    @Column(nullable = false)
    public String secretKey;

    /**
     * Heartbeat interval in seconds
     */
    @Column(columnDefinition = "integer default 30")
    public Integer heartbeatInterval = 30;

    /**
     * User defined configuration (YAML format)
     */
    @Column(columnDefinition = "TEXT")
    public String config;

    /**
     * Optional Gateway URL for agent connection (e.g. ws://192.168.1.100:8080)
     */
    public String gatewayUrl;

    /**
     * Port that Host Agent listens on
     */
    @Column(columnDefinition = "integer default 9090")
    public Integer listenPort = 9090;

    public String description;

    /**
     * Tags/metadata for categorizing hosts
     */
    @ElementCollection
    @CollectionTable(name = "infra_host_tags", joinColumns = @JoinColumn(name = "host_id"))
    @Column(name = "tag")
    public List<String> tags = new ArrayList<>();

    /**
     * Whether the host is enabled for operations
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    public boolean enabled = true;

    /**
     * Last time the host was seen/connected
     */
    public LocalDateTime lastSeenAt;

    /**
     * Last time the host reachability was checked
     */
    public LocalDateTime lastCheckedAt;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public LocalDateTime lastHeartbeat;

    /**
     * Associated agent instances
     */
    @OneToMany(mappedBy = "host")
    public List<AgentInstance> agentInstances;
}
