package com.easystation.diagnostic.dto;

import com.easystation.diagnostic.enums.DiagnosticCategory;
import com.easystation.diagnostic.enums.FindingSeverity;

import java.time.LocalDateTime;

/**
 * 诊断规则 DTO
 */
public class DiagnosticRuleRecord {
    
    public record Summary(
            String ruleId,
            String name,
            DiagnosticCategory category,
            FindingSeverity severity,
            boolean enabled
    ) {}
    
    public record Detail(
            String ruleId,
            String name,
            String description,
            DiagnosticCategory category,
            String condition,
            FindingSeverity severity,
            String recommendation,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
    
    public record Create(
            String name,
            String description,
            DiagnosticCategory category,
            String condition,
            FindingSeverity severity,
            String recommendation,
            boolean enabled
    ) {}
    
    public record Update(
            String name,
            String description,
            DiagnosticCategory category,
            String condition,
            FindingSeverity severity,
            String recommendation,
            Boolean enabled
    ) {}
}
