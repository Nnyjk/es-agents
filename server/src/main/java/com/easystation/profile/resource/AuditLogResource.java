package com.easystation.profile.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.service.AuditLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api/v1/me/audit-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "个人审计日志", description = "用户个人操作审计日志查询 API")
public class AuditLogResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    @Operation(summary = "列出审计日志", description = "分页查询当前用户的审计日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审计日志列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "action", description = "按操作类型过滤", required = false)
    @Parameter(name = "resourceType", description = "按资源类型过滤", required = false)
    @Parameter(name = "status", description = "按操作状态过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO-8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO-8601 格式）", required = false)
    @Parameter(name = "limit", description = "每页数量（默认 20）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("profile:read")
    public Response listLogs(
            @Context SecurityContext securityContext,
            @QueryParam("keyword") String keyword,
            @QueryParam("action") String action,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("status") String status,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        
        UUID userId = getCurrentUserId(securityContext);
        
        AuditLogRecord.Query query = new AuditLogRecord.Query(
            keyword,
            action,
            resourceType,
            status,
            startTime != null ? LocalDateTime.parse(startTime) : null,
            endTime != null ? LocalDateTime.parse(endTime) : null,
            limit,
            offset
        );
        
        return Response.ok(auditLogService.listLogs(userId, query)).build();
    }

    @GET
    @Path("/summary")
    @Operation(summary = "获取日志摘要", description = "获取当前用户审计日志的统计摘要")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回日志摘要"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response getSummary(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(auditLogService.getSummary(userId)).build();
    }

    private UUID getCurrentUserId(SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(securityContext.getUserPrincipal().getName());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid user identity", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
