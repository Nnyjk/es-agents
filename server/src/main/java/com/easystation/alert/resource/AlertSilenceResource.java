package com.easystation.alert.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.alert.dto.AlertSilenceRecord;
import com.easystation.alert.service.AlertSilenceService;
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
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Path("/api/v1/alerts/silences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "告警静默管理", description = "告警静默规则配置 API")
public class AlertSilenceResource {

    @Inject
    AlertSilenceService alertSilenceService;

    @GET
    @Operation(summary = "列出静默规则", description = "获取所有告警静默规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回静默规则列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:read")
    public Response list() {
        return Response.ok(alertSilenceService.list()).build();
    }

    @GET
    @Path("/enabled")
    @Operation(summary = "列出启用的静默规则", description = "获取所有已启用的告警静默规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回启用的静默规则列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:read")
    public Response listEnabled() {
        return Response.ok(alertSilenceService.listEnabled()).build();
    }

    @GET
    @Path("/active")
    @Operation(summary = "列出活动的静默规则", description = "获取指定时间点有效的静默规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回活动的静默规则列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "at", description = "查询时间点 (ISO 8601 格式)", required = false)
    @RequiresPermission("alert:read")
    public Response listActive(@QueryParam("at") String at) {
        LocalDateTime atTime = at != null ? LocalDateTime.parse(at, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : LocalDateTime.now();
        return Response.ok(alertSilenceService.listActive(atTime)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取静默规则", description = "根据 ID 查询静默规则详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回静默规则"),
        @APIResponse(responseCode = "404", description = "静默规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "静默规则 ID", required = true)
    @RequiresPermission("alert:read")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertSilenceService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建静默规则", description = "创建新的告警静默规则")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "静默规则创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:write")
    public Response create(@Valid AlertSilenceRecord.Create dto) {
        AlertSilenceRecord.Detail created = alertSilenceService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新静默规则", description = "更新告警静默规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "静默规则更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "静默规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "静默规则 ID", required = true)
    @RequiresPermission("alert:write")
    public Response update(@PathParam("id") UUID id, @Valid AlertSilenceRecord.Update dto) {
        return Response.ok(alertSilenceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除静默规则", description = "删除告警静默规则")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "静默规则删除成功"),
        @APIResponse(responseCode = "404", description = "静默规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "静默规则 ID", required = true)
    @RequiresPermission("alert:write")
    public Response delete(@PathParam("id") UUID id) {
        alertSilenceService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/check")
    @Operation(summary = "检查静默状态", description = "检查告警是否应该被静默")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回静默检查结果"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "eventType", description = "事件类型", required = false)
    @Parameter(name = "level", description = "告警级别", required = false)
    @Parameter(name = "source", description = "告警来源", required = false)
    @Parameter(name = "tags", description = "标签列表 (逗号分隔)", required = false)
    @RequiresPermission("alert:read")
    public Response checkSilence(
            @QueryParam("eventType") String eventType,
            @QueryParam("level") String level,
            @QueryParam("source") String source,
            @QueryParam("tags") String tags) {
        boolean silenced = alertSilenceService.shouldSilence(
                eventType,
                level,
                source,
                tags != null ? java.util.Arrays.asList(tags.split(",")) : null
        );
        return Response.ok(new SilenceCheckResult(silenced)).build();
    }

    public record SilenceCheckResult(boolean silenced) {}
}
