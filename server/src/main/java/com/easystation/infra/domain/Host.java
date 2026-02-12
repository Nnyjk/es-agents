package com.easystation.infra.domain;

import com.easystation.infra.domain.enums.HostStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "infra_host")
@Getter
@Setter
public class Host extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String hostname; // IP or Domain

    public String os;
    public String cpuInfo;
    public String memInfo;

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

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public LocalDateTime lastHeartbeat;
}
