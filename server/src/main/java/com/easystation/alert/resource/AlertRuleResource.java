package com.easystation.alert.resource;

import com.easystation.alert.dto.AlertRuleRecord;
import com.easystation.alert.service.AlertRuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/alerts/rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertRuleResource {

    @Inject
    AlertRuleService alertRuleService;

    @GET
    public Response list() {
        return Response.ok(alertRuleService.list()).build();
    }

    @GET
    @Path("/enabled")
    public Response listEnabled() {
        return Response.ok(alertRuleService.listEnabled()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(alertRuleService.get(id)).build();
    }

    @POST
    public Response create(@Valid AlertRuleRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(alertRuleService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AlertRuleRecord.Update dto) {
        return Response.ok(alertRuleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        alertRuleService.delete(id);
        return Response.noContent().build();
    }
}