package com.easystation.system.resource;

import com.easystation.system.record.ModuleActionRecord;
import com.easystation.system.record.ModuleRecord;
import com.easystation.system.service.ModuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModuleResource {

    @Inject
    ModuleService moduleService;

    @GET
    public Response list() {
        return Response.ok(moduleService.list()).build();
    }

    @POST
    public Response create(@Valid ModuleRecord dto) {
        return Response.status(Response.Status.CREATED).entity(moduleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, ModuleRecord dto) {
        return Response.ok(moduleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        moduleService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/actions")
    public Response createAction(@Valid ModuleActionRecord dto) {
        return Response.status(Response.Status.CREATED).entity(moduleService.createAction(dto)).build();
    }

    @DELETE
    @Path("/actions/{id}")
    public Response deleteAction(@PathParam("id") UUID id) {
        moduleService.deleteAction(id);
        return Response.noContent().build();
    }
}
