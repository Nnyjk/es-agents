package com.easystation.profile.resource;

import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.service.AuditLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/me/audit-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response listLogs(
            @Context SecurityContext securityContext,
            @QueryParam("keyword") String keyword,
            @QueryParam("action") String action,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("status") String status,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        
        UUID userId = getCurrentUserId(securityContext);
        
        AuditLogRecord.Query query = new AuditLogRecord.Query(
            keyword,
            action,
            resourceType,
            status,
            startTime != null ? LocalDateTime.parse(startTime) : null,
            endTime != null ? LocalDateTime.parse(endTime) : null,
            limit,
            offset
        );
        
        return Response.ok(auditLogService.listLogs(userId, query)).build();
    }

    @GET
    @Path("/summary")
    public Response getSummary(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(auditLogService.getSummary(userId)).build();
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