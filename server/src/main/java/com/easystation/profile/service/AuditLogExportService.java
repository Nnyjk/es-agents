package com.easystation.profile.service;

import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.repository.AuditLogRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * 审计日志导出服务
 * 
 * 支持格式：
 * - CSV
 * - JSON
 * - GZIP 压缩
 */
@ApplicationScoped
public class AuditLogExportService {

    @Inject
    AuditLogRepository auditLogRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出为 CSV 格式
     */
    public String exportToCSV(UUID userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<UserAuditLog> logs = auditLogRepository.findForExport(userId, startTime, endTime, limit);

        StringBuilder csv = new StringBuilder();
        
        // CSV 头部
        csv.append("ID,User ID,Action,Resource Type,Resource ID,Status,IP Address,")
           .append("Sensitive,Risk Level,Category,Duration (ms),Created At,Description\n");

        // CSV 数据
        for (UserAuditLog log : logs) {
            csv.append(escapeCsv(log.id.toString())).append(",")
               .append(escapeCsv(log.userId.toString())).append(",")
               .append(escapeCsv(log.action)).append(",")
               .append(escapeCsv(log.resourceType != null ? log.resourceType : "")).append(",")
               .append(escapeCsv(log.resourceId != null ? log.resourceId : "")).append(",")
               .append(escapeCsv(log.status)).append(",")
               .append(escapeCsv(log.ipAddress != null ? log.ipAddress : "")).append(",")
               .append(log.isSensitive).append(",")
               .append(escapeCsv(log.riskLevel)).append(",")
               .append(escapeCsv(log.operationCategory != null ? log.operationCategory : "")).append(",")
               .append(log.durationMs != null ? log.durationMs : "").append(",")
               .append(log.createdAt.format(DATE_FORMAT)).append(",")
               .append(escapeCsv(log.description != null ? log.description : ""))
               .append("\n");
        }

        return csv.toString();
    }

    /**
     * 导出为 JSON 格式
     */
    public String exportToJSON(UUID userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<UserAuditLog> logs = auditLogRepository.findForExport(userId, startTime, endTime, limit);

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"exportTime\": \"").append(LocalDateTime.now().format(DATE_FORMAT)).append("\",\n")
            .append("  \"userId\": \"").append(userId).append("\",\n")
            .append("  \"startTime\": \"").append(startTime.format(DATE_FORMAT)).append("\",\n")
            .append("  \"endTime\": \"").append(endTime.format(DATE_FORMAT)).append("\",\n")
            .append("  \"count\": ").append(logs.size()).append(",\n")
            .append("  \"logs\": [\n");

        for (int i = 0; i < logs.size(); i++) {
            UserAuditLog log = logs.get(i);
            json.append("    {\n")
                .append("      \"id\": \"").append(log.id).append("\",\n")
                .append("      \"userId\": \"").append(log.userId).append("\",\n")
                .append("      \"action\": \"").append(escapeJson(log.action)).append("\",\n")
                .append("      \"resourceType\": \"").append(escapeJson(log.resourceType != null ? log.resourceType : "")).append("\",\n")
                .append("      \"resourceId\": \"").append(log.resourceId != null ? log.resourceId : "").append("\",\n")
                .append("      \"status\": \"").append(escapeJson(log.status)).append("\",\n")
                .append("      \"ipAddress\": \"").append(escapeJson(log.ipAddress != null ? log.ipAddress : "")).append("\",\n")
                .append("      \"isSensitive\": ").append(log.isSensitive).append(",\n")
                .append("      \"riskLevel\": \"").append(escapeJson(log.riskLevel)).append("\",\n")
                .append("      \"operationCategory\": \"").append(escapeJson(log.operationCategory != null ? log.operationCategory : "")).append("\",\n")
                .append("      \"durationMs\": ").append(log.durationMs != null ? log.durationMs : "null").append(",\n")
                .append("      \"createdAt\": \"").append(log.createdAt.format(DATE_FORMAT)).append("\",\n")
                .append("      \"description\": \"").append(escapeJson(log.description != null ? log.description : "")).append("\"\n")
                .append("    }");
            
            if (i < logs.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n}");
        return json.toString();
    }

    /**
     * 导出为 GZIP 压缩的 CSV
     */
    public byte[] exportToGzipCSV(UUID userId, LocalDateTime startTime, LocalDateTime endTime, int limit) throws IOException {
        String csv = exportToCSV(userId, startTime, endTime, limit);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(csv.getBytes(StandardCharsets.UTF_8));
            gzos.finish();
            return baos.toByteArray();
        }
    }

    /**
     * 生成审计报告摘要
     */
    public AuditReportSummary generateSummary(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UserAuditLog> logs = auditLogRepository.findForExport(userId, startTime, endTime, 10000);

        AuditReportSummary summary = new AuditReportSummary();
        summary.totalLogs = logs.size();
        summary.sensitiveLogs = logs.stream().filter(l -> l.isSensitive).count();
        summary.failureLogs = logs.stream().filter(l -> "failure".equals(l.status)).count();
        summary.highRiskLogs = logs.stream().filter(l -> "HIGH".equals(l.riskLevel) || "CRITICAL".equals(l.riskLevel)).count();
        summary.uniqueIPs = logs.stream().map(l -> l.ipAddress).filter(ip -> ip != null).distinct().count();
        summary.topActions = logs.stream()
            .map(l -> l.action)
            .collect(java.util.stream.Collectors.groupingBy(a -> a, java.util.stream.Collectors.counting()))
            .entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(java.util.stream.Collectors.joining(", "));

        return summary;
    }

    /**
     * CSV 转义
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行，需要用引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * JSON 转义
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * 审计报告摘要
     */
    public static class AuditReportSummary {
        public long totalLogs;
        public long sensitiveLogs;
        public long failureLogs;
        public long highRiskLogs;
        public long uniqueIPs;
        public String topActions;
    }
}
