package com.easystation.diagnostic.service;

import com.easystation.diagnostic.domain.DiagnosticFinding;
import com.easystation.diagnostic.domain.DiagnosticReport;
import com.easystation.diagnostic.domain.DiagnosticRule;
import com.easystation.diagnostic.enums.FindingSeverity;
import com.easystation.diagnostic.enums.ReportStatus;
import com.easystation.monitoring.metrics.SystemMetrics;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 诊断引擎
 * 执行诊断规则并生成报告
 */
@ApplicationScoped
public class DiagnosticEngine {

    @Inject
    SystemMetrics systemMetrics;

    @Inject
    AlertDataCollector alertDataCollector;

    /**
     * 执行诊断
     * @param report 诊断报告
     * @return 诊断发现列表
     */
    @Transactional
    public List<DiagnosticFinding> runDiagnostic(DiagnosticReport report) {
        Log.infof("Starting diagnostic for report: %s", report.reportId);
        
        List<DiagnosticFinding> findings = new ArrayList<>();
        
        // 获取所有启用的规则
        List<DiagnosticRule> rules = DiagnosticRule.<DiagnosticRule>find("enabled", true).list();
        Log.infof("Found %d enabled rules to execute", rules.size());
        
        // 收集诊断数据
        Map<String, Object> context = collectDiagnosticContext();
        
        // 执行每个规则
        for (DiagnosticRule rule : rules) {
            try {
                DiagnosticFinding finding = evaluateRule(rule, report.reportId, context);
                if (finding != null) {
                    findings.add(finding);
                    finding.persist();
                }
            } catch (Exception e) {
                Log.warnf("Error evaluating rule %s: %s", rule.ruleId, e.getMessage());
            }
        }
        
        // 更新报告统计
        updateReportStatistics(report, findings);
        
        Log.infof("Diagnostic completed. Found %d issues", findings.size());
        return findings;
    }

    /**
     * 收集诊断上下文数据
     */
    private Map<String, Object> collectDiagnosticContext() {
        Map<String, Object> context = new HashMap<>();
        
        // 系统指标
        context.put("system.cpu.usage", systemMetrics.getCpuUsage());
        context.put("system.memory.usage", systemMetrics.getMemoryUsage());
        context.put("system.disk.usage", systemMetrics.getDiskUsage());
        
        // 告警统计
        context.put("alerts.count.24h", alertDataCollector.getAlertCountLast24Hours());
        context.put("alerts.count.critical", alertDataCollector.getCriticalAlertCount());
        
        // 时间戳
        context.put("timestamp", System.currentTimeMillis());
        
        return context;
    }

    /**
     * 评估单个规则
     */
    private DiagnosticFinding evaluateRule(DiagnosticRule rule, String reportId, Map<String, Object> context) {
        String condition = rule.condition;
        
        // 解析条件: metric > threshold
        Pattern pattern = Pattern.compile("(\\S+)\\s*(>|<|>=|<=|==|!=)\\s*(\\S+)");
        Matcher matcher = pattern.matcher(condition);
        
        if (!matcher.matches()) {
            Log.debugf("Rule %s condition format not supported: %s", rule.ruleId, condition);
            return null;
        }
        
        String metricName = matcher.group(1);
        String operator = matcher.group(2);
        double threshold = Double.parseDouble(matcher.group(3));
        
        Object valueObj = context.get(metricName);
        if (valueObj == null) {
            Log.debugf("Metric %s not found in context", metricName);
            return null;
        }
        
        double metricValue = ((Number) valueObj).doubleValue();
        boolean triggered = evaluateCondition(metricValue, operator, threshold);
        
        if (triggered) {
            return createFinding(rule, reportId, metricName, metricValue, threshold);
        }
        
        return null;
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(double value, String operator, double threshold) {
        return switch (operator) {
            case ">" -> value > threshold;
            case "<" -> value < threshold;
            case ">=" -> value >= threshold;
            case "<=" -> value <= threshold;
            case "==" -> value == threshold;
            case "!=" -> value != threshold;
            default -> false;
        };
    }

    /**
     * 创建诊断发现
     */
    private DiagnosticFinding createFinding(DiagnosticRule rule, String reportId, 
                                            String metricName, double metricValue, double threshold) {
        DiagnosticFinding finding = new DiagnosticFinding();
        finding.reportId = reportId;
        finding.ruleId = rule.ruleId;
        finding.title = rule.name;
        finding.description = rule.description;
        finding.severity = rule.severity;
        finding.metricName = metricName;
        finding.metricValue = metricValue;
        finding.thresholdValue = threshold;
        finding.impact = generateImpactDescription(rule, metricValue, threshold);
        finding.recommendation = rule.recommendation;
        return finding;
    }

    /**
     * 生成影响描述
     */
    private String generateImpactDescription(DiagnosticRule rule, double value, double threshold) {
        return String.format("当前值 %.2f 超过阈值 %.2f，可能影响系统稳定性。", value, threshold);
    }

    /**
     * 更新报告统计
     */
    @Transactional
    public void updateReportStatistics(DiagnosticReport report, List<DiagnosticFinding> findings) {
        report.totalFindings = findings.size();
        report.infoCount = (int) findings.stream().filter(f -> f.severity == FindingSeverity.INFO).count();
        report.warningCount = (int) findings.stream().filter(f -> f.severity == FindingSeverity.WARNING).count();
        report.criticalCount = (int) findings.stream().filter(f -> f.severity == FindingSeverity.CRITICAL).count();
        report.fatalCount = (int) findings.stream().filter(f -> f.severity == FindingSeverity.FATAL).count();
        report.status = ReportStatus.COMPLETED;
        report.completedAt = LocalDateTime.now();
        
        // 生成摘要
        report.summary = generateSummary(findings);
    }

    /**
     * 生成报告摘要
     */
    private String generateSummary(List<DiagnosticFinding> findings) {
        if (findings.isEmpty()) {
            return "系统运行正常，未发现问题。";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("诊断完成，共发现 %d 个问题。", findings.size()));
        
        Map<FindingSeverity, Long> counts = new EnumMap<>(FindingSeverity.class);
        for (FindingSeverity severity : FindingSeverity.values()) {
            counts.put(severity, findings.stream().filter(f -> f.severity == severity).count());
        }
        
        counts.forEach((severity, count) -> {
            if (count > 0) {
                sb.append(String.format(" %s: %d,", severity.getLabel(), count));
            }
        });
        
        return sb.toString();
    }
}
