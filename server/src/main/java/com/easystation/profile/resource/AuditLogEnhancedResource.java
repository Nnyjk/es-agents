package com.easystation.profile.resource;

import com.easystation.common.exception.UnauthorizedException;
import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.service.AuditLogEnhancedService;
import com.easystation.profile.service.AuditLogEnhancedService.AuditAnomaly;
import com.easystation.profile.service.AuditLogExportService;
import com.easystation.profile.service.AuditLogExportService.AuditReportSummary;
import com.easystation.profile.service.AuditLogService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 审计日志增强 API
 * 
 * 功能：
 * - 日志完整性验证
 * - 异常行为检测
 * - 审计报表导出
 */
@Path("/api/audit-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "AUDITOR"})
public class AuditLogEnhancedResource {

    @Inject
    AuditLogEnhancedService enhancedService;

    @Inject
    AuditLogExportService exportService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前登录用户 ID
     */
    private UUID getCurrentUserId() {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("未认证用户");
        }
        return UUID.fromString(principal.getName());
    }

    /**
     * 验证单条日志完整性
     */
    @GET
    @Path("/{id}/verify")
    public Response verifyLogIntegrity(@PathParam("id") UUID id) {
        UserAuditLog log = UserAuditLog.findById(id);
        if (log == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Log not found"))
                .build();
        }

        boolean isValid = enhancedService.verifyLogIntegrity(log);
        
        Map<String, Object> result = new HashMap<>();
        result.put("logId", id);
        result.put("isValid", isValid);
        result.put("verifiedAt", LocalDateTime.now().format(DATE_FORMAT));

        return Response.ok(result).build();
    }

    /**
     * 批量验证日志完整性
     */
    @POST
    @Path("/verify-batch")
    public Response verifyLogsIntegrity(List<UUID> logIds) {
        List<UserAuditLog> logs = UserAuditLog.find("id in ?1", logIds).list();
        Map<UUID, Boolean> results = enhancedService.verifyLogsIntegrity(logs);

        Map<String, Object> response = new HashMap<>();
        response.put("total", logIds.size());
        response.put("valid", results.values().stream().filter(v -> v).count());
        response.put("invalid", results.values().stream().filter(v -> !v).count());
        response.put("details", results);

        return Response.ok(response).build();
    }

    /**
     * 检测异常行为
     */
    @GET
    @Path("/anomalies")
    public Response detectAnomalies(
            @QueryParam("userId") UUID userId,
            @QueryParam("startTime") @DefaultValue("-7d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        UUID targetUserId = userId != null ? userId : getCurrentUserId();

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(targetUserId, start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", targetUserId);
        response.put("startTime", start.format(DATE_FORMAT));
        response.put("endTime", end.format(DATE_FORMAT));
        response.put("anomalyCount", anomalies.size());
        response.put("anomalies", anomalies);

        return Response.ok(response).build();
    }

    /**
     * 导出 CSV 格式审计日志
     */
    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportCSV(
            @QueryParam("userId") UUID userId,
            @QueryParam("startTime") @DefaultValue("-30d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime,
            @QueryParam("limit") @DefaultValue("10000") int limit) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        UUID targetUserId = userId != null ? userId : getCurrentUserId();

        String csv = exportService.exportToCSV(targetUserId, start, end, limit);

        String filename = String.format("audit_logs_%s_%s.csv",
            start.format(DateTimeFormatter.ISO_LOCAL_DATE),
            end.format(DateTimeFormatter.ISO_LOCAL_DATE));

        return Response.ok(csv, MediaType.TEXT_PLAIN)
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .build();
    }

    /**
     * 导出 JSON 格式审计日志
     */
    @GET
    @Path("/export/json")
    public Response exportJSON(
            @QueryParam("userId") UUID userId,
            @QueryParam("startTime") @DefaultValue("-30d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime,
            @QueryParam("limit") @DefaultValue("10000") int limit) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        UUID targetUserId = userId != null ? userId : getCurrentUserId();

        String json = exportService.exportToJSON(targetUserId, start, end, limit);

        return Response.ok(json, MediaType.APPLICATION_JSON)
            .header("Content-Disposition", "attachment; filename=\"audit_logs.json\"")
            .build();
    }

    /**
     * 导出 GZIP 压缩的 CSV
     */
    @GET
    @Path("/export/gzip")
    @Produces("application/gzip")
    public Response exportGzip(
            @QueryParam("userId") UUID userId,
            @QueryParam("startTime") @DefaultValue("-30d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime,
            @QueryParam("limit") @DefaultValue("10000") int limit) throws IOException {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        UUID targetUserId = userId != null ? userId : getCurrentUserId();

        byte[] gzipData = exportService.exportToGzipCSV(targetUserId, start, end, limit);

        String filename = String.format("audit_logs_%s_%s.csv.gz",
            start.format(DateTimeFormatter.ISO_LOCAL_DATE),
            end.format(DateTimeFormatter.ISO_LOCAL_DATE));

        return Response.ok(gzipData, "application/gzip")
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .build();
    }

    /**
     * 获取审计报告摘要
     */
    @GET
    @Path("/summary")
    public Response getSummary(
            @QueryParam("userId") UUID userId,
            @QueryParam("startTime") @DefaultValue("-30d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        UUID targetUserId = userId != null ? userId : getCurrentUserId();

        AuditReportSummary summary = exportService.generateSummary(targetUserId, start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", targetUserId);
        response.put("startTime", start.format(DATE_FORMAT));
        response.put("endTime", end.format(DATE_FORMAT));
        response.put("summary", summary);

        return Response.ok(response).build();
    }

    /**
     * 获取需要审查的日志列表
     */
    @GET
    @Path("/requires-review")
    @RolesAllowed({"ADMIN", "AUDITOR"})
    public Response getRequiresReview() {
        List<UserAuditLog> logs = UserAuditLog.find("requiresReview = true AND reviewStatus = 'PENDING'")
            .list();

        return Response.ok(logs).build();
    }

    /**
     * 审查日志
     */
    @POST
    @Path("/{id}/review")
    @RolesAllowed({"ADMIN", "AUDITOR"})
    public Response reviewLog(
            @PathParam("id") UUID id,
            ReviewRequest request) {
        
        UserAuditLog log = UserAuditLog.findById(id);
        if (log == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Log not found"))
                .build();
        }

        log.reviewStatus = request.status;
        log.reviewNotes = request.notes;
        log.reviewerId = getCurrentUserId();
        log.reviewedAt = LocalDateTime.now();
        log.persist();

        Map<String, Object> result = new HashMap<>();
        result.put("logId", id);
        result.put("reviewStatus", log.reviewStatus);
        result.put("reviewedAt", log.reviewedAt.format(DATE_FORMAT));
        result.put("reviewerId", getCurrentUserId());

        return Response.ok(result).build();
    }

    /**
     * 按风险等级查询日志
     */
    @GET
    @Path("/risk/{level}")
    public Response getByRiskLevel(
            @PathParam("level") String level,
            @QueryParam("startTime") @DefaultValue("-7d") String startTime,
            @QueryParam("endTime") @DefaultValue("now") String endTime) {
        
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);

        List<UserAuditLog> logs = UserAuditLog.find(
            "riskLevel = ?1 AND createdAt >= ?2 AND createdAt <= ?3 ORDER BY createdAt DESC",
            level, start, end
        ).list();

        return Response.ok(logs).build();
    }

    private LocalDateTime parseDateTime(String value) {
        if ("now".equals(value)) {
            return LocalDateTime.now();
        }
        if (value.startsWith("-") && value.endsWith("d")) {
            int days = Integer.parseInt(value.substring(1, value.length() - 1));
            return LocalDateTime.now().minusDays(days);
        }
        try {
            return LocalDateTime.parse(value, DATE_FORMAT);
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format: " + value);
        }
    }

    public static class ReviewRequest {
        public String status; // PENDING, REVIEWED, FLAGGED
        public String notes;
    }
}
