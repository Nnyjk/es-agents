package com.easystation.deployment.domain;

import com.easystation.deployment.enums.ApplicationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployment_application")
@Getter
@Setter
public class DeploymentApplication extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false)
    public String project;

    @Column(nullable = false)
    public String owner;

    @Column(name = "tech_stack", columnDefinition = "TEXT")
    public String techStack;

    @Column(name = "current_version")
    public String currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ApplicationStatus status = ApplicationStatus.ACTIVE;

    // Application Config fields
    @Column(name = "repository_url")
    public String repositoryUrl;

    @Column(name = "branch")
    public String branch;

    @Column(name = "build_script", columnDefinition = "TEXT")
    public String buildScript;

    @Column(name = "deploy_path")
    public String deployPath;

    @Column(name = "health_check_url")
    public String healthCheckUrl;

    @Column(name = "build_command", columnDefinition = "TEXT")
    public String buildCommand;

    @Column(name = "start_command", columnDefinition = "TEXT")
    public String startCommand;

    @Column(name = "stop_command", columnDefinition = "TEXT")
    public String stopCommand;

    @Column(columnDefinition = "TEXT")
    public String config;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "deleted")
    public boolean deleted = false;
}