package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.ChannelRecord;
import com.easystation.notification.service.NotificationChannelService;
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

@Path("/api/v1/notification-channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "通知渠道管理", description = "通知渠道配置与测试 API")
public class NotificationChannelResource {

    @Inject
    NotificationChannelService notificationChannelService;

    @GET
    @Operation(summary = "列出通知渠道", description = "获取所有通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知渠道列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(notificationChannelService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取通知渠道", description = "根据 ID 查询通知渠道详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知渠道"),
        @APIResponse(responseCode = "404", description = "通知渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知渠道 ID", required = true)
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationChannelService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建通知渠道", description = "创建新的通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "通知渠道创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:create")
    public Response create(@Valid ChannelRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(notificationChannelService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新通知渠道", description = "更新通知渠道配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "通知渠道更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "通知渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知渠道 ID", required = true)
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid ChannelRecord.Update dto) {
        return Response.ok(notificationChannelService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除通知渠道", description = "删除通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "通知渠道删除成功"),
        @APIResponse(responseCode = "404", description = "通知渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知渠道 ID", required = true)
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationChannelService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/test")
    @Operation(summary = "测试通知渠道", description = "发送测试消息验证通知渠道")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "测试成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "通知渠道不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知渠道 ID", required = true)
    @RequiresPermission("notification:test")
    public Response test(@PathParam("id") UUID id, @Valid ChannelRecord.TestRequest request) {
        return Response.ok(notificationChannelService.test(id, request)).build();
    }
}
