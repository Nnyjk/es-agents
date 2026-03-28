package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.ChannelRecord;
import com.easystation.notification.service.NotificationChannelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/notification-channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationChannelResource {

    @Inject
    NotificationChannelService notificationChannelService;

    @GET
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(notificationChannelService.list()).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationChannelService.get(id)).build();
    }

    @POST
    @RequiresPermission("notification:create")
    public Response create(@Valid ChannelRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(notificationChannelService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid ChannelRecord.Update dto) {
        return Response.ok(notificationChannelService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationChannelService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/test")
    @RequiresPermission("notification:test")
    public Response test(@PathParam("id") UUID id, @Valid ChannelRecord.TestRequest request) {
        return Response.ok(notificationChannelService.test(id, request)).build();
    }
}
