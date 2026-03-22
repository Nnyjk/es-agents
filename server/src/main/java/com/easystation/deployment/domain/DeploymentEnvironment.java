package com.easystation.deployment.domain;

import com.easystation.deployment.enums.EnvironmentType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_environment")
@Getter
@Setter
public class DeploymentEnvironment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment_type", nullable = false)
    public EnvironmentType environmentType = EnvironmentType.DEV;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "cluster_config", columnDefinition = "TEXT")
    public String clusterConfig;

    @Column(name = "resource_quota", columnDefinition = "TEXT")
    public String resourceQuota;

    @Column(columnDefinition = "TEXT")
    public String permissions;

    @Column(name = "config_center", columnDefinition = "TEXT")
    public String configCenter;

    @Column(name = "is_active", nullable = false)
    public Boolean active = true;

    @Column(name = "created_by")
    public String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}