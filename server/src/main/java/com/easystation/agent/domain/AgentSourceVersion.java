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
@Table(name = "agent_source_version")
@Getter
@Setter
public class AgentSourceVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "source_id", nullable = false)
    public UUID sourceId;

    @Column(nullable = false)
    public String version;

    @Column(name = "file_path")
    public String filePath;

    @Column(name = "file_size")
    public Long fileSize;

    @Column(name = "checksum_md5")
    public String checksumMd5;

    @Column(name = "checksum_sha256")
    public String checksumSha256;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "is_verified")
    public boolean verified;

    @Column(name = "verified_at")
    public LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    public String verifiedBy;

    @Column(name = "download_url")
    public String downloadUrl;

    @Column(name = "created_by")
    public String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}