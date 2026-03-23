package com.easystation.audit.resource;

import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/audit/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response list(
            @QueryParam("username") String username,
            @QueryParam("userId") UUID userId,
            @QueryParam("action") AuditAction action,
            @QueryParam("result") AuditResult result,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("resourceId") UUID resourceId,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        AuditRecord.Query query = new AuditRecord.Query(
                username, userId, action, result, resourceType, resourceId,
                startTime, endTime, keyword, limit, offset
        );
        return Response.ok(auditLogService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        AuditRecord.Detail detail = auditLogService.get(id);
        if (detail == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Audit log not found"))
                    .build();
        }
        return Response.ok(detail).build();
    }

    @POST
    public Response create(@Valid AuditRecord.Create dto) {
        auditLogService.record(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/summary")
    public Response getSummary() {
        return Response.ok(auditLogService.getSummary()).build();
    }

    @GET
    @Path("/stats/by-action")
    public Response countByAction(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        return Response.ok(auditLogService.countByAction(startTime, endTime)).build();
    }

    @GET
    @Path("/stats/by-user")
    public Response countByUser(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        return Response.ok(auditLogService.countByUser(startTime, endTime, limit)).build();
    }

    @GET
    @Path("/actions")
    public Response getActions() {
        List<ActionInfo> actions = List.of(AuditAction.values()).stream()
                .map(a -> new ActionInfo(a.name(), a.getDescription()))
                .toList();
        return Response.ok(actions).build();
    }

    // ==================== 导出接口 ====================

    @POST
    @Path("/export/json")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportJson(@Valid AuditRecord.ExportRequest request) {
        byte[] data = auditLogService.exportToJson(request);
        return Response.ok(data)
                .header("Content-Disposition", "attachment; filename=audit_logs.json")
                .build();
    }

    @POST
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportCsv(@Valid AuditRecord.ExportRequest request) {
        byte[] data = auditLogService.exportToCsv(request);
        return Response.ok(data)
                .header("Content-Disposition", "attachment; filename=audit_logs.csv")
                .build();
    }

    // ==================== 统计接口 ====================

    @GET
    @Path("/statistics/summary")
    public Response getStatisticsSummary(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : 
                LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : 
                LocalDateTime.now();
        return Response.ok(auditLogService.getStatisticsSummary(startTime, endTime)).build();
    }

    @GET
    @Path("/statistics/by-user")
    public Response getStatisticsByUser(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") @DefaultValue("10") Integer limit) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : 
                LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : 
                LocalDateTime.now();
        return Response.ok(auditLogService.getStatisticsByUser(startTime, endTime, limit)).build();
    }

    @GET
    @Path("/statistics/by-action")
    public Response getStatisticsByAction(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : 
                LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : 
                LocalDateTime.now();
        return Response.ok(auditLogService.getStatisticsByAction(startTime, endTime)).build();
    }

    @GET
    @Path("/statistics/by-date")
    public Response getStatisticsByDate(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : 
                LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : 
                LocalDateTime.now();
        return Response.ok(auditLogService.getStatisticsByDate(startTime, endTime)).build();
    }

    @GET
    @Path("/statistics/by-hour")
    public Response getStatisticsByHour(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : 
                LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : 
                LocalDateTime.now();
        return Response.ok(auditLogService.getStatisticsByHour(startTime, endTime)).build();
    }

    // ==================== 归档接口 ====================

    @POST
    @Path("/archive")
    public Response archiveLogs(@Valid AuditRecord.ArchiveRequest request) {
        LocalDateTime beforeDate = request.beforeDate() != null ? 
                request.beforeDate() : LocalDateTime.now().minusDays(90);
        Boolean includeSuccess = request.includeSuccess() != null ? 
                request.includeSuccess() : true;
        Boolean includeFailed = request.includeFailed() != null ? 
                request.includeFailed() : true;

        AuditRecord.ArchiveResult result = auditLogService.archiveLogs(
                beforeDate, includeSuccess, includeFailed);
        return Response.ok(result).build();
    }

    // ==================== 清理接口 ====================

    @POST
    @Path("/cleanup")
    public Response cleanupLogs(@Valid AuditRecord.CleanupRequest request) {
        LocalDateTime beforeDate = request.beforeDate() != null ? 
                request.beforeDate() : LocalDateTime.now().minusDays(365);
        Boolean dryRun = request.dryRun() != null ? request.dryRun() : false;

        AuditRecord.CleanupResult result = auditLogService.cleanupLogs(beforeDate, dryRun);
        return Response.ok(result).build();
    }

    record ActionInfo(String name, String description) {}
}