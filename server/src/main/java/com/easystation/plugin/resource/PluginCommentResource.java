package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginCommentRecord;
import com.easystation.plugin.service.PluginCommentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/api/v1/plugin-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginCommentResource {

    @Inject
    PluginCommentService commentService;

    @POST
    public Response create(@Valid PluginCommentRecord.Create create, @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(commentService.create(create, userId))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return commentService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    public Response findByPluginId(
            @PathParam("pluginId") UUID pluginId) {
        return Response.ok(commentService.findByPluginId(pluginId)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PluginCommentRecord.Update update) {
        return Response.ok(commentService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        commentService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/reply")
    public Response reply(
            @PathParam("id") UUID id,
            @Valid PluginCommentRecord.Create reply,
            @Context SecurityContext securityContext) {
        // Create a new comment as a reply
        PluginCommentRecord.Create replyCreate = new PluginCommentRecord.Create(
                reply.pluginId(),
                id, // parentId is the comment being replied to
                reply.replyToUserId(),
                reply.content()
        );
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(commentService.create(replyCreate, userId))
                .build();
    }

    @GET
    @Path("/{id}/replies")
    public Response getReplies(@PathParam("id") UUID id) {
        return Response.ok(commentService.findReplies(id)).build();
    }

    private UUID getUserId(SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try {
                return UUID.fromString(securityContext.getUserPrincipal().getName());
            } catch (IllegalArgumentException e) {
                // Return a default user ID for development/testing
                return UUID.randomUUID();
            }
        }
        // Return a default user ID for development/testing
        return UUID.randomUUID();
    }
}