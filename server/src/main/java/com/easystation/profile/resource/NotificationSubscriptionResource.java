package com.easystation.profile.resource;

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

import java.util.List;
import java.util.UUID;

@Path("/me/notification-subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationSubscriptionResource {

    @Inject
    NotificationSubscriptionService subscriptionService;

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response listSubscriptions(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(subscriptionService.listSubscriptions(userId)).build();
    }

    @PUT
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