package com.easystation.agent.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_source_cache")
@Getter
@Setter
public class AgentSourceCache extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "source_id", nullable = false)
    public UUID sourceId;

    @Column(name = "version_id")
    public UUID versionId;

    @Column(name = "cache_path", nullable = false)
    public String cachePath;

    @Column(name = "cache_size")
    public Long cacheSize;

    @Column(name = "is_valid")
    public boolean valid;

    @Column(name = "last_accessed_at")
    public LocalDateTime lastAccessedAt;

    @Column(name = "expires_at")
    public LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}