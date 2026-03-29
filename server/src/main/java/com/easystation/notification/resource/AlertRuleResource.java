package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.AlertRuleRecord;
import com.easystation.notification.service.AlertRuleService;
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

import java.util.UUID;

@Path("/api/v1/alert-rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "告警规则管理", description = "告警通知规则配置 API")
public class AlertRuleResource {

    @Inject
    AlertRuleService alertRuleService;

    @GET
    @Operation(summary = "列出告警规则", description = "获取所有告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警规则列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(alertRuleService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取告警规则", description = "根据 ID 查询告警规则详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警规则"),
        @APIResponse(responseCode = "404", description = "告警规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警规则 ID", required = true)
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建告警规则", description = "创建新的告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "告警规则创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:create")
    public Response create(@Valid AlertRuleRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(alertRuleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新告警规则", description = "更新告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警规则更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "告警规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警规则 ID", required = true)
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid AlertRuleRecord.Update dto) {
        return Response.ok(alertRuleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除告警规则", description = "删除告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "告警规则删除成功"),
        @APIResponse(responseCode = "404", description = "告警规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警规则 ID", required = true)
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        alertRuleService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/enable")
    @Operation(summary = "启用告警规则", description = "启用告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警规则启用成功"),
        @APIResponse(responseCode = "404", description = "告警规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警规则 ID", required = true)
    @RequiresPermission("notification:edit")
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.enable(id)).build();
    }

    @POST
    @Path("/{id}/disable")
    @Operation(summary = "禁用告警规则", description = "禁用告警通知规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警规则禁用成功"),
        @APIResponse(responseCode = "404", description = "告警规则不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "告警规则 ID", required = true)
    @RequiresPermission("notification:edit")
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.disable(id)).build();
    }
}
