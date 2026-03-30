package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.ReportStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断报告实体
 */
@Entity
@Table(name = "diagnostic_reports")
public class DiagnosticReport extends PanacheEntity {
    
    @Column(name = "report_id", unique = true, nullable = false, length = 36)
    public String reportId;
    
    @Column(nullable = false, length = 200)
    public String title;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReportStatus status;
    
    @Column(name = "started_at")
    public LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    public LocalDateTime completedAt;
    
    @Column(name = "total_findings")
    public int totalFindings;
    
    @Column(name = "info_count")
    public int infoCount;
    
    @Column(name = "warning_count")
    public int warningCount;
    
    @Column(name = "critical_count")
    public int criticalCount;
    
    @Column(name = "fatal_count")
    public int fatalCount;
    
    @Column(columnDefinition = "TEXT")
    public String summary;
    
    @Column(name = "created_by", length = 100)
    public String createdBy;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @PrePersist
    void prePersist() {
        if (reportId == null) reportId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        if (startedAt == null) startedAt = LocalDateTime.now();
    }
}
