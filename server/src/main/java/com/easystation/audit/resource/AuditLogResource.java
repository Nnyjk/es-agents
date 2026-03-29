package com.easystation.audit.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/audit/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "审计日志管理", description = "审计日志查询、导出、统计 API")
public class AuditLogResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    @Operation(summary = "列出审计日志", description = "分页查询审计日志列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审计日志列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "username", description = "按用户名过滤", required = false)
    @Parameter(name = "userId", description = "按用户 ID 过滤", required = false)
    @Parameter(name = "action", description = "按操作类型过滤", required = false)
    @Parameter(name = "result", description = "按操作结果过滤", required = false)
    @Parameter(name = "resourceType", description = "按资源类型过滤", required = false)
    @Parameter(name = "resourceId", description = "按资源 ID 过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "获取审计日志", description = "根据 ID 查询审计日志详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审计日志"),
        @APIResponse(responseCode = "404", description = "审计日志不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "审计日志 ID", required = true)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "创建审计日志", description = "记录审计日志")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "审计日志创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:write")
    public Response create(@Valid AuditRecord.Create dto) {
        auditLogService.record(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/summary")
    @Operation(summary = "获取摘要统计", description = "获取审计日志摘要统计信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回摘要统计"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:read")
    public Response getSummary() {
        return Response.ok(auditLogService.getSummary()).build();
    }

    @GET
    @Path("/stats/by-action")
    @Operation(summary = "按操作类型统计", description = "按操作类型统计审计日志数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @RequiresPermission("audit:read")
    public Response countByAction(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        return Response.ok(auditLogService.countByAction(startTime, endTime)).build();
    }

    @GET
    @Path("/stats/by-user")
    @Operation(summary = "按用户统计", description = "按用户统计审计日志数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "limit", description = "返回数量限制", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "获取操作类型列表", description = "获取所有审计操作类型及其描述")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回操作类型列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:read")
    public Response getActions() {
        List<ActionInfo> actions = List.of(AuditAction.values()).stream()
                .map(a -> new ActionInfo(a.name(), a.getDescription()))
                .toList();
        return Response.ok(actions).build();
    }

    // ==================== 导出接口 ====================

    @POST
    @Path("/export/json")
    @Operation(summary = "导出 JSON", description = "导出审计日志为 JSON 格式")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "导出成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RequiresPermission("audit:export")
    public Response exportJson(@Valid AuditRecord.ExportRequest request) {
        byte[] data = auditLogService.exportToJson(request);
        return Response.ok(data)
                .header("Content-Disposition", "attachment; filename=audit_logs.json")
                .build();
    }

    @POST
    @Path("/export/csv")
    @Operation(summary = "导出 CSV", description = "导出审计日志为 CSV 格式")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "导出成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Produces("text/csv")
    @RequiresPermission("audit:export")
    public Response exportCsv(@Valid AuditRecord.ExportRequest request) {
        byte[] data = auditLogService.exportToCsv(request);
        return Response.ok(data)
                .header("Content-Disposition", "attachment; filename=audit_logs.csv")
                .build();
    }

    // ==================== 统计接口 ====================

    @GET
    @Path("/statistics/summary")
    @Operation(summary = "统计摘要", description = "获取审计日志统计摘要")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计摘要"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "按用户统计", description = "按用户统计审计日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "limit", description = "返回数量限制", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "按操作类型统计", description = "按操作类型统计审计日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "按日期统计", description = "按日期统计审计日志趋势")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "按小时统计", description = "按小时统计审计日志分布")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @RequiresPermission("audit:read")
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
    @Operation(summary = "归档日志", description = "归档指定时间前的审计日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "归档成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:admin")
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
    @Operation(summary = "清理日志", description = "清理指定时间前的审计日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "清理成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:admin")
    public Response cleanupLogs(@Valid AuditRecord.CleanupRequest request) {
        LocalDateTime beforeDate = request.beforeDate() != null ? 
                request.beforeDate() : LocalDateTime.now().minusDays(365);
        Boolean dryRun = request.dryRun() != null ? request.dryRun() : false;

        AuditRecord.CleanupResult result = auditLogService.cleanupLogs(beforeDate, dryRun);
        return Response.ok(result).build();
    }

    record ActionInfo(String name, String description) {}
}
