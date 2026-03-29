package com.easystation.alert.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.alert.dto.AlertChannelRecord;
import com.easystation.alert.service.AlertChannelService;
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

@Path("/api/v1/alerts/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "告警渠道管理", description = "告警通知渠道配置 API")
public class AlertChannelResource {

    @Inject
    AlertChannelService alertChannelService;

    @GET
    @Operation(summary = "列出渠道", description = "获取所有告警通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回渠道列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:read")
    public Response list() {
        return Response.ok(alertChannelService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取渠道", description = "根据 ID 查询告警渠道详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回渠道信息"),
        @APIResponse(responseCode = "404", description = "渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "渠道 ID", required = true)
    @RequiresPermission("alert:read")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertChannelService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建渠道", description = "创建新的告警通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "渠道创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("alert:write")
    public Response create(@Valid AlertChannelRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(alertChannelService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新渠道", description = "更新告警渠道配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "渠道更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "渠道 ID", required = true)
    @RequiresPermission("alert:write")
    public Response update(@PathParam("id") UUID id, @Valid AlertChannelRecord.Update dto) {
        return Response.ok(alertChannelService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除渠道", description = "删除告警渠道")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "渠道删除成功"),
        @APIResponse(responseCode = "404", description = "渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "渠道 ID", required = true)
    @RequiresPermission("alert:write")
    public Response delete(@PathParam("id") UUID id) {
        alertChannelService.delete(id);
        return Response.noContent().build();
    }
}
