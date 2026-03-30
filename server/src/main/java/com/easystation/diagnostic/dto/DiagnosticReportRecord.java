package com.easystation.diagnostic.dto;

import com.easystation.diagnostic.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 诊断报告 DTO
 */
public class DiagnosticReportRecord {
    
    public record Detail(
            String reportId,
            String title,
            ReportStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            int totalFindings,
            int infoCount,
            int warningCount,
            int criticalCount,
            int fatalCount,
            String summary,
            String createdBy,
            LocalDateTime createdAt
    ) {}
    
    public record WithFindings(
            String reportId,
            String title,
            ReportStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            int totalFindings,
            int infoCount,
            int warningCount,
            int criticalCount,
            int fatalCount,
            String summary,
            String createdBy,
            LocalDateTime createdAt,
            List<DiagnosticFindingRecord.Detail> findings
    ) {}
    
    public record Summary(
            String reportId,
            String title,
            ReportStatus status,
            int totalFindings,
            int warningCount,
            int criticalCount,
            int fatalCount,
            LocalDateTime createdAt
    ) {}
    
    public record Generate(
            String title,
            String createdBy
    ) {}
}
