package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.service.PluginReviewService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/api/v1/plugin-reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginReviewResource {

    @Inject
    PluginReviewService reviewService;

    @POST
    public Response submit(@Valid PluginReviewRecord.Submit submit) {
        return Response.status(Response.Status.CREATED)
                .entity(reviewService.submit(submit))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return reviewService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    public Response findByPluginId(
            @PathParam("pluginId") UUID pluginId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(reviewService.findByPluginId(pluginId, status, page, size)).build();
    }

    @GET
    @Path("/version/{versionId}")
    public Response findByVersionId(
            @PathParam("versionId") UUID versionId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(reviewService.findByVersionId(versionId, status, page, size)).build();
    }

    @GET
    @Path("/pending")
    public Response getPendingReviews(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(reviewService.getPendingReviews(page, size)).build();
    }

    @POST
    @Path("/{id}/approve")
    public Response approve(
            @PathParam("id") UUID id,
            @QueryParam("comment") String comment) {
        return Response.ok(reviewService.approve(id, comment)).build();
    }

    @POST
    @Path("/{id}/reject")
    public Response reject(
            @PathParam("id") UUID id,
            @QueryParam("reason") String reason) {
        return Response.ok(reviewService.reject(id, reason)).build();
    }

    @POST
    @Path("/{id}/comment")
    public Response addComment(
            @PathParam("id") UUID id,
            @QueryParam("comment") String comment) {
        return Response.ok(reviewService.addComment(id, comment)).build();
    }

    @GET
    @Path("/{id}/history")
    public Response getHistory(@PathParam("id") UUID id) {
        return Response.ok(reviewService.getHistory(id)).build();
    }
}