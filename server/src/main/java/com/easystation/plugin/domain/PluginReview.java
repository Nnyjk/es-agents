package com.easystation.plugin.domain;

import com.easystation.plugin.domain.enums.ReviewStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plugin_review")
@Getter
@Setter
public class PluginReview extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(name = "version_id")
    public UUID versionId;

    @Column(name = "reviewer_id", nullable = false)
    public UUID reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "review_type", length = 20)
    public String reviewType;

    @Column(columnDefinition = "TEXT")
    public String comment;

    @Column(name = "security_check_result", columnDefinition = "TEXT")
    public String securityCheckResult;

    @Column(name = "compatibility_check_result", columnDefinition = "TEXT")
    public String compatibilityCheckResult;

    @Column(name = "test_report", columnDefinition = "TEXT")
    public String testReport;

    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}