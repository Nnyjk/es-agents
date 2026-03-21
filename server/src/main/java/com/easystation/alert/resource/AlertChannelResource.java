package com.easystation.alert.resource;

import com.easystation.alert.dto.AlertChannelRecord;
import com.easystation.alert.service.AlertChannelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/alerts/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertChannelResource {

    @Inject
    AlertChannelService alertChannelService;

    @GET
    public Response list() {
        return Response.ok(alertChannelService.list()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertChannelService.get(id)).build();
    }

    @POST
    public Response create(@Valid AlertChannelRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(alertChannelService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AlertChannelRecord.Update dto) {
        return Response.ok(alertChannelService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        alertChannelService.delete(id);
        return Response.noContent().build();
    }
}