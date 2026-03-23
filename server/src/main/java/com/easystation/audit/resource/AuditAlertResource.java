package com.easystation.audit.resource;

import com.easystation.audit.domain.AuditAlertConfig;
import com.easystation.audit.domain.AuditAlertHistory;
import com.easystation.audit.service.AuditAlertService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 审计告警管理接口
 */
@Path("/audit/alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditAlertResource {

    @Inject
    AuditAlertService auditAlertService;

    // ==================== 告警配置管理 ====================

    @POST
    @Path("/configs")
    public Response createConfig(@Valid AuditAlertConfig config) {
        AuditAlertConfig created = auditAlertService.createConfig(config);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/configs/{id}")
    public Response updateConfig(@PathParam("id") UUID id, @Valid AuditAlertConfig config) {
        AuditAlertConfig updated = auditAlertService.updateConfig(id, config);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/configs/{id}")
    public Response deleteConfig(@PathParam("id") UUID id) {
        auditAlertService.deleteConfig(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/configs/{id}")
    public Response getConfig(@PathParam("id") UUID id) {
        return auditAlertService.getConfig(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/configs")
    public Response listConfigs(
            @QueryParam("alertType") String alertType,
            @QueryParam("enabled") Boolean enabled,
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        Map<String, Object> result = auditAlertService.listConfigs(alertType, enabled, page, size);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/configs/{id}/toggle")
    public Response toggleConfig(@PathParam("id") UUID id, @QueryParam("enabled") boolean enabled) {
        auditAlertService.toggleConfig(id, enabled);
        return Response.ok().build();
    }

    // ==================== 告警历史查询 ====================

    @GET
    @Path("/history")
    public Response listHistory(
            @QueryParam("alertType") String alertType,
            @QueryParam("status") String status,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("20") Integer size) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        Map<String, Object> result = auditAlertService.listHistory(alertType, status, startTime, endTime, page, size);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/history/{id}/acknowledge")
    public Response acknowledgeAlert(
            @PathParam("id") UUID id,
            @QueryParam("acknowledgedBy") String acknowledgedBy,
            @QueryParam("remark") String remark) {
        auditAlertService.acknowledgeAlert(id, acknowledgedBy, remark);
        return Response.ok().build();
    }

    @PUT
    @Path("/history/{id}/resolve")
    public Response resolveAlert(
            @PathParam("id") UUID id,
            @QueryParam("remark") String remark) {
        auditAlertService.resolveAlert(id, remark);
        return Response.ok().build();
    }
}