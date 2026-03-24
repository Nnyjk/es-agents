package com.easystation.plugin.resource;

import com.easystation.plugin.domain.enums.ReviewStatus;
import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.service.PluginReviewService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/plugin-reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginReviewResource {

    @Inject
    PluginReviewService reviewService;

    @POST
    @Path("/submit")
    public Response submit(@Valid PluginReviewRecord.Submit submit) {
        return Response.status(Response.Status.CREATED)
                .entity(reviewService.submit(submit))
                .build();
    }

    @POST
    @Path("/{reviewId}/approve")
    public Response approve(@PathParam("reviewId") UUID reviewId, @Valid PluginReviewRecord.Approve approve) {
        return Response.ok(reviewService.approve(reviewId, approve)).build();
    }

    @POST
    @Path("/{reviewId}/reject")
    public Response reject(@PathParam("reviewId") UUID reviewId, @Valid PluginReviewRecord.Reject reject) {
        return Response.ok(reviewService.reject(reviewId, reject)).build();
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
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(reviewService.findByPluginId(pluginId)).build();
    }

    @GET
    @Path("/status/{status}")
    public Response findByStatus(@PathParam("status") ReviewStatus status) {
        return Response.ok(reviewService.findByStatus(status)).build();
    }

    @GET
    @Path("/plugin/{pluginId}/pending")
    public Response findPendingByPluginId(@PathParam("pluginId") UUID pluginId) {
        return reviewService.findPendingByPluginId(pluginId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/search")
    public Response search(@BeanParam PluginReviewRecord.Query query) {
        return Response.ok(reviewService.search(query)).build();
    }

    @GET
    @Path("/count/status/{status}")
    public Response countByStatus(@PathParam("status") ReviewStatus status) {
        return Response.ok(reviewService.countByStatus(status)).build();
    }

    @GET
    @Path("/count/plugin/{pluginId}")
    public Response countByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(reviewService.countByPluginId(pluginId)).build();
    }
}