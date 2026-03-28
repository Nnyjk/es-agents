package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.HistoryRecord;
import com.easystation.notification.service.NotificationHistoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/notification-history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationHistoryResource {

    @Inject
    NotificationHistoryService notificationHistoryService;

    @GET
    @RequiresPermission("notification:view")
    public Response list(@BeanParam HistoryRecord.Query query) {
        return Response.ok(notificationHistoryService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationHistoryService.get(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationHistoryService.delete(id);
        return Response.noContent().build();
    }
}
