package com.easystation.alert.resource;

import com.easystation.alert.dto.AlertSilenceRecord;
import com.easystation.alert.service.AlertSilenceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Path("/alerts/silences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertSilenceResource {

    @Inject
    AlertSilenceService alertSilenceService;

    @GET
    public Response list() {
        return Response.ok(alertSilenceService.list()).build();
    }

    @GET
    @Path("/enabled")
    public Response listEnabled() {
        return Response.ok(alertSilenceService.listEnabled()).build();
    }

    @GET
    @Path("/active")
    public Response listActive(@QueryParam("at") String at) {
        LocalDateTime atTime = at != null ? LocalDateTime.parse(at, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : LocalDateTime.now();
        return Response.ok(alertSilenceService.listActive(atTime)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertSilenceService.get(id)).build();
    }

    @POST
    public Response create(@Valid AlertSilenceRecord.Create dto) {
        AlertSilenceRecord.Detail created = alertSilenceService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AlertSilenceRecord.Update dto) {
        return Response.ok(alertSilenceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        alertSilenceService.delete(id);
        return Response.noContent().build();
    }

    /**
     * 检查告警是否应该被静默
     */
    @GET
    @Path("/check")
    public Response checkSilence(
            @QueryParam("eventType") String eventType,
            @QueryParam("level") String level,
            @QueryParam("source") String source,
            @QueryParam("tags") String tags) {
        boolean silenced = alertSilenceService.shouldSilence(
                eventType,
                level,
                source,
                tags != null ? java.util.Arrays.asList(tags.split(",")) : null
        );
        return Response.ok(new SilenceCheckResult(silenced)).build();
    }

    public record SilenceCheckResult(boolean silenced) {}
}