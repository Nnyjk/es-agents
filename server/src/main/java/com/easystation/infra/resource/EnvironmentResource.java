package com.easystation.infra.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.infra.record.EnvironmentRecord;
import com.easystation.infra.service.EnvironmentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @GET
    @RequiresPermission("environment:view")
    public Response list() {
        return Response.ok(environmentService.list()).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("environment:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(environmentService.get(id)).build();
    }

    @POST
    @RequiresPermission("environment:create")
    public Response create(@Valid EnvironmentRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(environmentService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("environment:edit")
    public Response update(@PathParam("id") UUID id, @Valid EnvironmentRecord.Update dto) {
        return Response.ok(environmentService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("environment:delete")
    public Response delete(@PathParam("id") UUID id) {
        environmentService.delete(id);
        return Response.noContent().build();
    }
}
