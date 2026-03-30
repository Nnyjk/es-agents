package com.easystation.diagnostic.dto;

import com.easystation.diagnostic.enums.FindingSeverity;

import java.time.LocalDateTime;

/**
 * 诊断发现 DTO
 */
public class DiagnosticFindingRecord {
    
    public record Detail(
            String findingId,
            String reportId,
            String ruleId,
            String title,
            String description,
            FindingSeverity severity,
            String metricName,
            Double metricValue,
            Double thresholdValue,
            String impact,
            String recommendation,
            LocalDateTime createdAt
    ) {}
    
    public record Summary(
            String findingId,
            String title,
            FindingSeverity severity,
            String metricName,
            Double metricValue
    ) {}
}
