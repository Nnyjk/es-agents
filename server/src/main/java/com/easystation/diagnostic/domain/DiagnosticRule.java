package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.DiagnosticCategory;
import com.easystation.diagnostic.enums.FindingSeverity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断规则实体
 */
@Entity
@Table(name = "diagnostic_rules")
public class DiagnosticRule extends PanacheEntity {
    
    @Column(name = "rule_id", unique = true, nullable = false, length = 36)
    public String ruleId;
    
    @Column(nullable = false, length = 100)
    public String name;
    
    @Column(length = 500)
    public String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DiagnosticCategory category;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    public String condition;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FindingSeverity severity;
    
    @Column(columnDefinition = "TEXT")
    public String recommendation;
    
    public boolean enabled = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (ruleId == null) ruleId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
