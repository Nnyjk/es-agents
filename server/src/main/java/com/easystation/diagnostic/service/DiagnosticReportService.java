package com.easystation.diagnostic.service;

import com.easystation.diagnostic.domain.DiagnosticFinding;
import com.easystation.diagnostic.domain.DiagnosticReport;
import com.easystation.diagnostic.dto.DiagnosticFindingRecord;
import com.easystation.diagnostic.dto.DiagnosticReportRecord;
import com.easystation.diagnostic.enums.ReportStatus;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 诊断报告服务
 */
@ApplicationScoped
public class DiagnosticReportService {

    @Inject
    DiagnosticEngine diagnosticEngine;

    public List<DiagnosticReportRecord.Summary> list() {
        return DiagnosticReport.<DiagnosticReport>listAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<DiagnosticReportRecord.Summary> listRecent(int limit) {
        return DiagnosticReport.<DiagnosticReport>find("ORDER BY createdAt DESC")
                .stream()
                .limit(limit)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public DiagnosticReportRecord.WithFindings get(String reportId) {
        DiagnosticReport report = DiagnosticReport.find("reportId", reportId).firstResult();
        if (report == null) {
            throw new WebApplicationException("Report not found: " + reportId, Response.Status.NOT_FOUND);
        }
        
        List<DiagnosticFindingRecord.Detail> findings = DiagnosticFinding
                .<DiagnosticFinding>find("reportId", reportId)
                .stream()
                .map(this::toFindingDetail)
                .collect(Collectors.toList());
        
        return new DiagnosticReportRecord.WithFindings(
                report.reportId,
                report.title,
                report.status,
                report.startedAt,
                report.completedAt,
                report.totalFindings,
                report.infoCount,
                report.warningCount,
                report.criticalCount,
                report.fatalCount,
                report.summary,
                report.createdBy,
                report.createdAt,
                findings
        );
    }

    @Transactional
    public DiagnosticReportRecord.Detail generate(DiagnosticReportRecord.Generate request) {
        Log.infof("Generating diagnostic report: %s", request.title());
        
        // 创建报告实体
        DiagnosticReport report = new DiagnosticReport();
        report.title = request.title();
        report.status = ReportStatus.GENERATING;
        report.createdBy = request.createdBy();
        report.persist();
        
        // 执行诊断
        List<DiagnosticFinding> findings = diagnosticEngine.runDiagnostic(report);
        
        return toDetail(report);
    }

    @Transactional
    public DiagnosticReportRecord.Detail generateAsync(DiagnosticReportRecord.Generate request) {
        DiagnosticReport report = new DiagnosticReport();
        report.title = request.title();
        report.status = ReportStatus.GENERATING;
        report.createdBy = request.createdBy();
        report.persist();
        
        // 在实际实现中，这里应该触发异步任务
        // 目前简化为同步执行
        diagnosticEngine.runDiagnostic(report);
        
        return toDetail(report);
    }

    @Transactional
    public void delete(String reportId) {
        // 先删除关联的 findings
        DiagnosticFinding.delete("reportId", reportId);
        
        // 再删除报告
        long deleted = DiagnosticReport.delete("reportId", reportId);
        if (deleted == 0) {
            throw new WebApplicationException("Report not found: " + reportId, Response.Status.NOT_FOUND);
        }
    }

    private DiagnosticReportRecord.Detail toDetail(DiagnosticReport report) {
        return new DiagnosticReportRecord.Detail(
                report.reportId,
                report.title,
                report.status,
                report.startedAt,
                report.completedAt,
                report.totalFindings,
                report.infoCount,
                report.warningCount,
                report.criticalCount,
                report.fatalCount,
                report.summary,
                report.createdBy,
                report.createdAt
        );
    }

    private DiagnosticReportRecord.Summary toSummary(DiagnosticReport report) {
        return new DiagnosticReportRecord.Summary(
                report.reportId,
                report.title,
                report.status,
                report.totalFindings,
                report.warningCount,
                report.criticalCount,
                report.fatalCount,
                report.createdAt
        );
    }

    private DiagnosticFindingRecord.Detail toFindingDetail(DiagnosticFinding finding) {
        return new DiagnosticFindingRecord.Detail(
                finding.findingId,
                finding.reportId,
                finding.ruleId,
                finding.title,
                finding.description,
                finding.severity,
                finding.metricName,
                finding.metricValue,
                finding.thresholdValue,
                finding.impact,
                finding.recommendation,
                finding.createdAt
        );
    }
}
