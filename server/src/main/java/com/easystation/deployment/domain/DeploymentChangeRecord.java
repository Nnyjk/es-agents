package com.easystation.deployment.domain;

import com.easystation.deployment.enums.ChangeType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署变更记录实体
 */
@Entity
@Table(name = "deployment_change_record", indexes = {
    @Index(name = "idx_change_version", columnList = "version_id"),
    @Index(name = "idx_change_app", columnList = "application_id"),
    @Index(name = "idx_change_type", columnList = "change_type"),
    @Index(name = "idx_change_created", columnList = "created_at")
})
@Getter
@Setter
public class DeploymentChangeRecord extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "version_id", nullable = false)
    public UUID versionId;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    public ChangeType changeType;

    @Column(name = "change_key")
    public String changeKey;

    @Column(name = "change_title")
    public String changeTitle;

    @Column(name = "old_value", columnDefinition = "TEXT")
    public String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    public String newValue;

    @Column(name = "change_diff", columnDefinition = "TEXT")
    public String changeDiff;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "impact_level")
    public Integer impactLevel = 1;

    @Column(name = "impact_scope", columnDefinition = "TEXT")
    public String impactScope;

    @Column(name = "risk_level")
    public Integer riskLevel = 1;

    @Column(name = "risk_description", columnDefinition = "TEXT")
    public String riskDescription;

    @Column(name = "commit_hash")
    public String commitHash;

    @Column(name = "commit_url")
    public String commitUrl;

    @Column(name = "author")
    public String author;

    @Column(name = "author_email")
    public String authorEmail;

    @Column(name = "related_issue")
    public String relatedIssue;

    @Column(name = "related_pr")
    public String relatedPr;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "created_by")
    public String createdBy;
}