package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.AlertRuleRecord;
import com.easystation.notification.service.AlertRuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/alert-rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertRuleResource {

    @Inject
    AlertRuleService alertRuleService;

    @GET
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(alertRuleService.list()).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.get(id)).build();
    }

    @POST
    @RequiresPermission("notification:create")
    public Response create(@Valid AlertRuleRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(alertRuleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid AlertRuleRecord.Update dto) {
        return Response.ok(alertRuleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        alertRuleService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/enable")
    @RequiresPermission("notification:edit")
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.enable(id)).build();
    }

    @POST
    @Path("/{id}/disable")
    @RequiresPermission("notification:edit")
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.disable(id)).build();
    }
}
