package com.easystation.agent.resource;

import com.easystation.agent.dto.SystemEventLogDTO;
import com.easystation.agent.dto.SystemEventLogDTO.EventLogPage;
import com.easystation.agent.dto.SystemEventLogDTO.EventQueryCriteria;
import com.easystation.agent.service.SystemEventLogService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;

/**
 * 系统事件日志 API
 */
@Path("/v1/system-event-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "System Event Logs", description = "系统事件日志管理")
public class SystemEventLogResource {

    @Inject
    SystemEventLogService eventLogService;

    /**
     * 记录事件（内部调用）
     * POST /v1/system-event-logs
     */
    @POST
    @RequiresPermission("system:admin")
    @Operation(summary = "记录系统事件", description = "记录系统或业务事件日志")
    public Response logEvent(@Valid SystemEventLogDTO dto) {
        SystemEventLogDTO result = eventLogService.logEvent(dto);
        return Response.ok(result).build();
    }

    /**
     * 查询事件日志列表
     * GET /v1/system-event-logs
     */
    @GET
    @RequiresPermission("system:view")
    @Operation(summary = "查询事件日志", description = "支持多维度筛选查询")
    public Response queryEvents(
            @Parameter(description = "事件类型") @QueryParam("eventType") String eventType,
            @Parameter(description = "事件级别") @QueryParam("eventLevel") String eventLevel,
            @Parameter(description = "模块名称") @QueryParam("module") String module,
            @Parameter(description = "操作类型") @QueryParam("operation") String operation,
            @Parameter(description = "目标类型") @QueryParam("targetType") String targetType,
            @Parameter(description = "目标 ID") @QueryParam("targetId") Long targetId,
            @Parameter(description = "用户 ID") @QueryParam("userId") Long userId,
            @Parameter(description = "开始时间") @QueryParam("startTime") LocalDateTime startTime,
            @Parameter(description = "结束时间") @QueryParam("endTime") LocalDateTime endTime,
            @Parameter(description = "关键词") @QueryParam("keyword") String keyword,
            @Parameter(description = "页码") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "每页大小") @QueryParam("size") @DefaultValue("20") int size) {
        
        EventQueryCriteria criteria = new EventQueryCriteria(
            eventType, eventLevel, module, operation, targetType, targetId, userId,
            startTime, endTime, keyword
        );
        
        EventLogPage result = eventLogService.queryEvents(criteria, page, size);
        return Response.ok(result).build();
    }

    /**
     * 获取单个事件详情
     * GET /v1/system-event-logs/{id}
     */
    @GET
    @Path("/{id}")
    @RequiresPermission("system:view")
    @Operation(summary = "获取事件详情", description = "根据 ID 获取单个事件日志详情")
    public Response getEventById(@PathParam("id") Long id) {
        SystemEventLogDTO result = eventLogService.getEventById(id);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Event log not found")
                    .build();
        }
        return Response.ok(result).build();
    }

    /**
     * 清理旧日志
     * DELETE /v1/system-event-logs
     */
    @DELETE
    @RequiresPermission("system:admin")
    @Operation(summary = "清理旧日志", description = "清理指定天数之前的日志")
    public Response cleanupOldLogs(
            @Parameter(description = "保留天数") @QueryParam("retentionDays") @DefaultValue("30") int retentionDays) {
        long deleted = eventLogService.cleanupOldLogs(retentionDays);
        return Response.ok(new DeleteResult(deleted)).build();
    }

    /**
     * 删除结果
     */
    public record DeleteResult(long deletedCount) {}
}
