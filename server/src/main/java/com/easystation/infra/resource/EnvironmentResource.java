package com.easystation.infra.resource;

import com.easystation.infra.record.EnvironmentRecord;
import com.easystation.infra.service.EnvironmentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/infra/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @GET
    public Response list() {
        return Response.ok(environmentService.list()).build();
    }

    @POST
    public Response create(@Valid EnvironmentRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(environmentService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, EnvironmentRecord.Update dto) {
        return Response.ok(environmentService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        environmentService.delete(id);
        return Response.noContent().build();
    }
}
