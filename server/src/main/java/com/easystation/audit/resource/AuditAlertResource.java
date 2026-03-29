package com.easystation.audit.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.domain.AuditAlertConfig;
import com.easystation.audit.domain.AuditAlertHistory;
import com.easystation.audit.service.AuditAlertService;
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
import java.util.Map;
import java.util.UUID;

/**
 * 审计告警管理接口
 */
@Path("/api/v1/audit/alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "审计告警管理", description = "审计告警配置与历史查询 API")
public class AuditAlertResource {

    @Inject
    AuditAlertService auditAlertService;

    // ==================== 告警配置管理 ====================

    @POST
    @Path("/configs")
    @Operation(summary = "创建告警配置", description = "创建新的审计告警配置")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "告警配置创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("audit:admin")
    public Response createConfig(@Valid AuditAlertConfig config) {
        AuditAlertConfig created = auditAlertService.createConfig(config);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/configs/{id}")
    @Operation(summary = "更新告警配置", description = "更新审计告警配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警配置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "告警配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警配置 ID", required = true)
    @RequiresPermission("audit:admin")
    public Response updateConfig(@PathParam("id") UUID id, @Valid AuditAlertConfig config) {
        AuditAlertConfig updated = auditAlertService.updateConfig(id, config);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/configs/{id}")
    @Operation(summary = "删除告警配置", description = "删除审计告警配置")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "告警配置删除成功"),
        @APIResponse(responseCode = "404", description = "告警配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警配置 ID", required = true)
    @RequiresPermission("audit:admin")
    public Response deleteConfig(@PathParam("id") UUID id) {
        auditAlertService.deleteConfig(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/configs/{id}")
    @Operation(summary = "获取告警配置", description = "根据 ID 查询告警配置详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警配置"),
        @APIResponse(responseCode = "404", description = "告警配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警配置 ID", required = true)
    @RequiresPermission("audit:read")
    public Response getConfig(@PathParam("id") UUID id) {
        return auditAlertService.getConfig(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/configs")
    @Operation(summary = "列出告警配置", description = "分页查询告警配置列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警配置列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "alertType", description = "按告警类型过滤", required = false)
    @Parameter(name = "enabled", description = "按启用状态过滤", required = false)
    @Parameter(name = "page", description = "页码", required = false)
    @Parameter(name = "size", description = "每页数量", required = false)
    @RequiresPermission("audit:read")
    public Response listConfigs(
            @QueryParam("alertType") String alertType,
            @QueryParam("enabled") Boolean enabled,
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        Map<String, Object> result = auditAlertService.listConfigs(alertType, enabled, page, size);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/configs/{id}/toggle")
    @Operation(summary = "切换告警配置状态", description = "启用或禁用告警配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态切换成功"),
        @APIResponse(responseCode = "404", description = "告警配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警配置 ID", required = true)
    @Parameter(name = "enabled", description = "启用状态", required = true)
    @RequiresPermission("audit:admin")
    public Response toggleConfig(@PathParam("id") UUID id, @QueryParam("enabled") boolean enabled) {
        auditAlertService.toggleConfig(id, enabled);
        return Response.ok().build();
    }

    // ==================== 告警历史查询 ====================

    @GET
    @Path("/history")
    @Operation(summary = "列出告警历史", description = "分页查询告警历史记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警历史列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "alertType", description = "按告警类型过滤", required = false)
    @Parameter(name = "status", description = "按告警状态过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "page", description = "页码", required = false)
    @Parameter(name = "size", description = "每页数量", required = false)
    @RequiresPermission("audit:read")
    public Response listHistory(
            @QueryParam("alertType") String alertType,
            @QueryParam("status") String status,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        Map<String, Object> result = auditAlertService.listHistory(alertType, status, startTime, endTime, page, size);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/history/{id}/acknowledge")
    @Operation(summary = "确认告警", description = "确认审计告警")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警确认成功"),
        @APIResponse(responseCode = "404", description = "告警历史不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警历史 ID", required = true)
    @Parameter(name = "acknowledgedBy", description = "确认人", required = false)
    @Parameter(name = "remark", description = "备注", required = false)
    @RequiresPermission("audit:write")
    public Response acknowledgeAlert(
            @PathParam("id") UUID id,
            @QueryParam("acknowledgedBy") String acknowledgedBy,
            @QueryParam("remark") String remark) {
        auditAlertService.acknowledgeAlert(id, acknowledgedBy, remark);
        return Response.ok().build();
    }

    @PUT
    @Path("/history/{id}/resolve")
    @Operation(summary = "解决告警", description = "解决审计告警")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警解决成功"),
        @APIResponse(responseCode = "404", description = "告警历史不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警历史 ID", required = true)
    @Parameter(name = "remark", description = "备注", required = false)
    @RequiresPermission("audit:write")
    public Response resolveAlert(
            @PathParam("id") UUID id,
            @QueryParam("remark") String remark) {
        auditAlertService.resolveAlert(id, remark);
        return Response.ok().build();
    }
}
