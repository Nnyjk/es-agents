package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginRatingRecord;
import com.easystation.plugin.service.PluginRatingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/api/v1/plugin-ratings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginRatingResource {

    @Inject
    PluginRatingService ratingService;

    @POST
    public Response create(@Valid PluginRatingRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(ratingService.create(create))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return ratingService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    public Response findByPluginId(
            @PathParam("pluginId") UUID pluginId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(ratingService.findByPluginId(pluginId, page, size)).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response findByUserId(@PathParam("userId") UUID userId) {
        return Response.ok(ratingService.findByUserId(userId)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PluginRatingRecord.Update update) {
        return Response.ok(ratingService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        ratingService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/plugin/{pluginId}/stats")
    public Response getPluginRatingStats(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(ratingService.getPluginRatingStats(pluginId)).build();
    }
}