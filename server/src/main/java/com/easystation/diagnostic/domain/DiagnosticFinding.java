package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.FindingSeverity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断发现实体
 */
@Entity
@Table(name = "diagnostic_findings")
public class DiagnosticFinding extends PanacheEntity {
    
    @Column(name = "finding_id", unique = true, nullable = false, length = 36)
    public String findingId;
    
    @Column(name = "report_id", nullable = false, length = 36)
    public String reportId;
    
    @Column(name = "rule_id", length = 36)
    public String ruleId;
    
    @Column(nullable = false, length = 200)
    public String title;
    
    @Column(columnDefinition = "TEXT")
    public String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FindingSeverity severity;
    
    @Column(name = "metric_name", length = 100)
    public String metricName;
    
    @Column(name = "metric_value")
    public Double metricValue;
    
    @Column(name = "threshold_value")
    public Double thresholdValue;
    
    @Column(columnDefinition = "TEXT")
    public String impact;
    
    @Column(columnDefinition = "TEXT")
    public String recommendation;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @PrePersist
    void prePersist() {
        if (findingId == null) findingId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
