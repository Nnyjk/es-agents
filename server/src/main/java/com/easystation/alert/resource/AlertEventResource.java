package com.easystation.alert.resource;

import com.easystation.alert.dto.AlertEventRecord;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import com.easystation.alert.service.AlertEventService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/alerts/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertEventResource {

    @Inject
    AlertEventService alertEventService;

    @GET
    public Response list(
            @QueryParam("eventType") AlertEventType eventType,
            @QueryParam("level") AlertLevel level,
            @QueryParam("status") AlertStatus status,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("resourceId") UUID resourceId,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        AlertEventRecord.Query query = new AlertEventRecord.Query(
                eventType, level, status, environmentId, resourceId, resourceType, keyword, limit, offset
        );
        return Response.ok(alertEventService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertEventService.get(id)).build();
    }

    @POST
    public Response create(@Valid AlertEventRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(alertEventService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}/acknowledge")
    public Response acknowledge(@PathParam("id") UUID id, @Valid AlertEventRecord.Acknowledge dto) {
        return Response.ok(alertEventService.acknowledge(id, dto)).build();
    }

    @PUT
    @Path("/{id}/resolve")
    public Response resolve(@PathParam("id") UUID id, @Valid AlertEventRecord.Resolve dto) {
        return Response.ok(alertEventService.resolve(id, dto)).build();
    }

    @PUT
    @Path("/{id}/ignore")
    public Response ignore(@PathParam("id") UUID id) {
        return Response.ok(alertEventService.ignore(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        alertEventService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/counts")
    public Response counts() {
        return Response.ok(Map.of(
                "pending", alertEventService.countByStatus(AlertStatus.PENDING),
                "notified", alertEventService.countByStatus(AlertStatus.NOTIFIED),
                "acknowledged", alertEventService.countByStatus(AlertStatus.ACKNOWLEDGED),
                "resolved", alertEventService.countByStatus(AlertStatus.RESOLVED),
                "ignored", alertEventService.countByStatus(AlertStatus.IGNORED)
        )).build();
    }
}