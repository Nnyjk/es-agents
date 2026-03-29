package com.easystation.alert.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.alert.dto.AlertEventRecord;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import com.easystation.alert.service.AlertEventService;
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

import java.util.Map;
import java.util.UUID;

@Path("/api/v1/alerts/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "告警事件管理", description = "告警事件查询、确认、解决 API")
public class AlertEventResource {

    @Inject
    AlertEventService alertEventService;

    @GET
    @Operation(summary = "列出告警事件", description = "分页查询告警事件列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警事件列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "eventType", description = "按事件类型过滤", required = false)
    @Parameter(name = "level", description = "按告警级别过滤", required = false)
    @Parameter(name = "status", description = "按告警状态过滤", required = false)
    @Parameter(name = "environmentId", description = "按环境 ID 过滤", required = false)
    @Parameter(name = "resourceId", description = "按资源 ID 过滤", required = false)
    @Parameter(name = "resourceType", description = "按资源类型过滤", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
    @RequiresPermission("alert:read")
    public Response list(
            @QueryParam("eventType") AlertEventType eventType,
            @QueryParam("level") AlertLevel level,
            @QueryParam("status") AlertStatus status,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("resourceId") UUID resourceId,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        AlertEventRecord.Query query = new AlertEventRecord.Query(
                eventType, level, status, environmentId, resourceId, resourceType, keyword, limit, offset
        );
        return Response.ok(alertEventService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取告警事件", description = "根据 ID 查询告警事件详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警事件"),
        @APIResponse(responseCode = "404", description = "告警事件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警事件 ID", required = true)
    @RequiresPermission("alert:read")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertEventService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建告警事件", description = "创建新的告警事件")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "告警事件创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:write")
    public Response create(@Valid AlertEventRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(alertEventService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}/acknowledge")
    @Operation(summary = "确认告警", description = "确认告警事件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警确认成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "告警事件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警事件 ID", required = true)
    @RequiresPermission("alert:write")
    public Response acknowledge(@PathParam("id") UUID id, @Valid AlertEventRecord.Acknowledge dto) {
        return Response.ok(alertEventService.acknowledge(id, dto)).build();
    }

    @PUT
    @Path("/{id}/resolve")
    @Operation(summary = "解决告警", description = "解决告警事件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警解决成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "告警事件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警事件 ID", required = true)
    @RequiresPermission("alert:write")
    public Response resolve(@PathParam("id") UUID id, @Valid AlertEventRecord.Resolve dto) {
        return Response.ok(alertEventService.resolve(id, dto)).build();
    }

    @PUT
    @Path("/{id}/ignore")
    @Operation(summary = "忽略告警", description = "忽略告警事件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警忽略成功"),
        @APIResponse(responseCode = "404", description = "告警事件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警事件 ID", required = true)
    @RequiresPermission("alert:write")
    public Response ignore(@PathParam("id") UUID id) {
        return Response.ok(alertEventService.ignore(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除告警事件", description = "删除告警事件")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "告警事件删除成功"),
        @APIResponse(responseCode = "404", description = "告警事件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警事件 ID", required = true)
    @RequiresPermission("alert:write")
    public Response delete(@PathParam("id") UUID id) {
        alertEventService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/counts")
    @Operation(summary = "获取状态统计", description = "获取各状态告警事件的数量统计")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:read")
    public Response counts() {
        return Response.ok(Map.of(
                "pending", alertEventService.countByStatus(AlertStatus.PENDING),
                "notified", alertEventService.countByStatus(AlertStatus.NOTIFIED),
                "acknowledged", alertEventService.countByStatus(AlertStatus.ACKNOWLEDGED),
                "resolved", alertEventService.countByStatus(AlertStatus.RESOLVED),
                "ignored", alertEventService.countByStatus(AlertStatus.IGNORED)
        )).build();
    }
}
