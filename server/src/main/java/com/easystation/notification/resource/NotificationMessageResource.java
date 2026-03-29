package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.NotificationRecord;
import com.easystation.notification.enums.MessageLevel;
import com.easystation.notification.enums.MessageType;
import com.easystation.notification.service.NotificationMessageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 站内消息通知 API
 */
@Path("/api/v1/notification/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "站内消息通知", description = "站内消息查询、管理、统计 API")
public class NotificationMessageResource {

    @Inject
    NotificationMessageService notificationMessageService;

    @POST
    @Operation(summary = "创建消息", description = "创建一条新的站内消息")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "消息创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:write")
    public Response create(
            @Valid @RequestBody(description = "消息创建请求") NotificationRecord.Create dto
    ) {
        var message = notificationMessageService.create(dto);
        return Response.status(Response.Status.CREATED).entity(message).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取消息详情", description = "根据 ID 查询站内消息详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回消息详情"),
        @APIResponse(responseCode = "404", description = "消息不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "消息 ID", required = true)
    @RequiresPermission("notification:read")
    public Response get(@PathParam("id") UUID id) {
        var detail = notificationMessageService.findById(id);
        if (detail == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Message not found"))
                    .build();
        }
        return Response.ok(detail).build();
    }

    @GET
    @Operation(summary = "列出消息", description = "分页查询站内消息列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回消息列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "userId", description = "按用户 ID 过滤", required = false)
    @Parameter(name = "type", description = "按消息类型过滤", required = false)
    @Parameter(name = "level", description = "按消息级别过滤", required = false)
    @Parameter(name = "isRead", description = "按已读状态过滤", required = false)
    @Parameter(name = "relatedType", description = "按关联资源类型过滤", required = false)
    @Parameter(name = "relatedId", description = "按关联资源 ID 过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
    @RequiresPermission("notification:read")
    public Response list(
            @QueryParam("userId") UUID userId,
            @QueryParam("type") MessageType type,
            @QueryParam("level") MessageLevel level,
            @QueryParam("isRead") Boolean isRead,
            @QueryParam("relatedType") String relatedType,
            @QueryParam("relatedId") UUID relatedId,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset
    ) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        NotificationRecord.Query query = new NotificationRecord.Query(
                userId, type, level, isRead, relatedType, relatedId,
                startTime, endTime, keyword, limit, offset
        );
        List<NotificationRecord.ListItem> list = notificationMessageService.findAll(query);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}/read")
    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "标记成功"),
        @APIResponse(responseCode = "404", description = "消息不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "消息 ID", required = true)
    @RequiresPermission("notification:write")
    public Response markAsRead(@PathParam("id") UUID id) {
        boolean success = notificationMessageService.markAsRead(id);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Message not found or already read"))
                    .build();
        }
        return Response.ok(Map.of("success", true)).build();
    }

    @PUT
    @Path("/read-batch")
    @Operation(summary = "批量标记已读", description = "批量将多条消息标记为已读状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "批量标记成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:write")
    public Response markBatchAsRead(
            @RequestBody(description = "消息 ID 列表") List<UUID> ids
    ) {
        int count = notificationMessageService.markBatchAsRead(ids);
        return Response.ok(Map.of("count", count)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除消息", description = "删除指定的站内消息（软删除）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "删除成功"),
        @APIResponse(responseCode = "404", description = "消息不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "消息 ID", required = true)
    @RequiresPermission("notification:write")
    public Response delete(@PathParam("id") UUID id) {
        boolean success = notificationMessageService.delete(id);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Message not found or already deleted"))
                    .build();
        }
        return Response.ok(Map.of("success", true)).build();
    }

    @DELETE
    @Path("/batch")
    @Operation(summary = "批量删除消息", description = "批量删除多条站内消息（软删除）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "批量删除成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:write")
    public Response deleteBatch(
            @RequestBody(description = "消息 ID 列表") List<UUID> ids
    ) {
        int count = notificationMessageService.deleteBatch(ids);
        return Response.ok(Map.of("count", count)).build();
    }

    @GET
    @Path("/unread-count")
    @Operation(summary = "获取未读数量", description = "获取当前用户的未读消息数量统计")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回未读数量"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "userId", description = "用户 ID", required = true)
    @RequiresPermission("notification:read")
    public Response getUnreadCount(@QueryParam("userId") UUID userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "userId is required"))
                    .build();
        }
        var unreadCount = notificationMessageService.getUnreadCount(userId);
        return Response.ok(unreadCount).build();
    }

    @GET
    @Path("/statistics")
    @Operation(summary = "获取消息统计", description = "获取当前用户的消息统计数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计数据"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "userId", description = "用户 ID", required = true)
    @RequiresPermission("notification:read")
    public Response getStatistics(@QueryParam("userId") UUID userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "userId is required"))
                    .build();
        }
        var statistics = notificationMessageService.getStatistics(userId);
        return Response.ok(statistics).build();
    }
}
