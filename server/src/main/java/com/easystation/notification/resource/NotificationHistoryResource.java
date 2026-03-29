package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.HistoryRecord;
import com.easystation.notification.service.NotificationHistoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/v1/notification-history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "通知历史管理", description = "通知发送历史查询 API")
public class NotificationHistoryResource {

    @Inject
    NotificationHistoryService notificationHistoryService;

    @GET
    @Operation(summary = "列出通知历史", description = "分页查询通知发送历史")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知历史列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "channelId", description = "按渠道 ID 过滤", required = false)
    @Parameter(name = "status", description = "按发送状态过滤", required = false)
    @Parameter(name = "templateId", description = "按模板 ID 过滤", required = false)
    @Parameter(name = "recipient", description = "按接收者过滤", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
    @RequiresPermission("notification:view")
    public Response list(@BeanParam HistoryRecord.Query query) {
        return Response.ok(notificationHistoryService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取通知历史", description = "根据 ID 查询通知历史详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知历史"),
        @APIResponse(responseCode = "404", description = "通知历史不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知历史 ID", required = true)
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationHistoryService.get(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除通知历史", description = "删除通知历史记录")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "通知历史删除成功"),
        @APIResponse(responseCode = "404", description = "通知历史不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知历史 ID", required = true)
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationHistoryService.delete(id);
        return Response.noContent().build();
    }
}
