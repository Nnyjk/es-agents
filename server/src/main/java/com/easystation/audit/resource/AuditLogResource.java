package com.easystation.audit.resource;

import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/audit/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response list(
            @QueryParam("username") String username,
            @QueryParam("userId") UUID userId,
            @QueryParam("action") AuditAction action,
            @QueryParam("result") AuditResult result,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("resourceId") UUID resourceId,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        AuditRecord.Query query = new AuditRecord.Query(
                username, userId, action, result, resourceType, resourceId,
                startTime, endTime, keyword, limit, offset
        );
        return Response.ok(auditLogService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        AuditRecord.Detail detail = auditLogService.get(id);
        if (detail == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Audit log not found"))
                    .build();
        }
        return Response.ok(detail).build();
    }

    @POST
    public Response create(@Valid AuditRecord.Create dto) {
        auditLogService.record(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/summary")
    public Response getSummary() {
        return Response.ok(auditLogService.getSummary()).build();
    }

    @GET
    @Path("/stats/by-action")
    public Response countByAction(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        return Response.ok(auditLogService.countByAction(startTime, endTime)).build();
    }

    @GET
    @Path("/stats/by-user")
    public Response countByUser(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        return Response.ok(auditLogService.countByUser(startTime, endTime, limit)).build();
    }

    @GET
    @Path("/actions")
    public Response getActions() {
        List<ActionInfo> actions = List.of(AuditAction.values()).stream()
                .map(a -> new ActionInfo(a.name(), a.getDescription()))
                .toList();
        return Response.ok(actions).build();
    }

    record ActionInfo(String name, String description) {}
}