package com.easystation.profile.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.profile.dto.NotificationSubscriptionRecord;
import com.easystation.profile.service.AuditLogService;
import com.easystation.profile.service.NotificationSubscriptionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/me/notification-subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "通知订阅管理", description = "用户通知渠道订阅配置 API")
public class NotificationSubscriptionResource {

    @Inject
    NotificationSubscriptionService subscriptionService;

    @Inject
    AuditLogService auditLogService;

    @GET
    @Operation(summary = "列出订阅", description = "获取当前用户的所有通知订阅配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回订阅列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response listSubscriptions(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(subscriptionService.listSubscriptions(userId)).build();
    }

    @PUT
    @Operation(summary = "更新订阅", description = "更新单个通知订阅配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "订阅更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
    public Response updateSubscription(
            @Context SecurityContext securityContext,
            @Valid NotificationSubscriptionRecord.Update dto) {
        UUID userId = getCurrentUserId(securityContext);
        
        NotificationSubscriptionRecord result = subscriptionService.updateSubscription(userId, dto);
        
        auditLogService.logSuccess(
            userId, "NOTIFICATION_SUBSCRIPTION_UPDATE", "USER_NOTIFICATION_SUBSCRIPTION", null,
            "Updated notification subscription: " + dto.notificationType(), 
            null, null, null
        );
        
        return Response.ok(result).build();
    }

    @PUT
    @Path("/batch")
    @Operation(summary = "批量更新订阅", description = "批量更新多个通知订阅配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "订阅批量更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
    public Response batchUpdate(
            @Context SecurityContext securityContext,
            @Valid NotificationSubscriptionRecord.BatchUpdate dto) {
        UUID userId = getCurrentUserId(securityContext);
        
        List<NotificationSubscriptionRecord> result = subscriptionService.batchUpdate(userId, dto);
        
        auditLogService.logSuccess(
            userId, "NOTIFICATION_SUBSCRIPTION_BATCH_UPDATE", "USER_NOTIFICATION_SUBSCRIPTION", null,
            "Batch updated notification subscriptions", 
            null, null, null
        );
        
        return Response.ok(result).build();
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
