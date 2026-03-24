package com.easystation.plugin.domain;

import com.easystation.plugin.domain.enums.InstallationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plugin_installation")
@Getter
@Setter
public class PluginInstallation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(name = "version_id")
    public UUID versionId;

    @Column(name = "agent_id")
    public UUID agentId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public InstallationStatus status = InstallationStatus.INSTALLING;

    @Column(name = "installed_version", length = 20)
    public String installedVersion;

    @Column(name = "config_data", columnDefinition = "TEXT")
    public String configData;

    @Column(name = "install_path", length = 500)
    public String installPath;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "last_started_at")
    public LocalDateTime lastStartedAt;

    @Column(name = "last_stopped_at")
    public LocalDateTime lastStoppedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}