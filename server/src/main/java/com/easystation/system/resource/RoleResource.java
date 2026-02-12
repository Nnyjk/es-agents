package com.easystation.system.resource;

import com.easystation.system.record.RoleRecord;
import com.easystation.system.service.RoleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    RoleService roleService;

    @GET
    public Response list() {
        return Response.ok(roleService.list()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(roleService.get(id)).build();
    }

    @POST
    public Response create(@Valid RoleRecord dto) {
        return Response.status(Response.Status.CREATED).entity(roleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, RoleRecord dto) {
        return Response.ok(roleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        roleService.delete(id);
        return Response.noContent().build();
    }
}
