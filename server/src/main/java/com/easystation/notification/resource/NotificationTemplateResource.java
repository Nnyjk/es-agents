package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.TemplateRecord;
import com.easystation.notification.service.NotificationTemplateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/notification-templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationTemplateResource {

    @Inject
    NotificationTemplateService notificationTemplateService;

    @GET
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(notificationTemplateService.list()).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationTemplateService.get(id)).build();
    }

    @POST
    @RequiresPermission("notification:create")
    public Response create(@Valid TemplateRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(notificationTemplateService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid TemplateRecord.Update dto) {
        return Response.ok(notificationTemplateService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationTemplateService.delete(id);
        return Response.noContent().build();
    }
}
