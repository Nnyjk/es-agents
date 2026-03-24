package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginCategoryRecord;
import com.easystation.plugin.service.PluginCategoryService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/plugin-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginCategoryResource {

    @Inject
    PluginCategoryService categoryService;

    @POST
    public Response create(@Valid PluginCategoryRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(categoryService.create(create))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return categoryService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    public Response findAll() {
        return Response.ok(categoryService.findAll()).build();
    }

    @GET
    @Path("/tree")
    public Response getTree() {
        return Response.ok(categoryService.getCategoryTree()).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PluginCategoryRecord.Update update) {
        return Response.ok(categoryService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        categoryService.delete(id);
        return Response.noContent().build();
    }
}