package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginRecord;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.easystation.plugin.service.PluginService;
import io.quarkus.panache.common.Page;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/api/v1/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginResource {

    @Inject
    PluginService pluginService;

    @POST
    public Response create(@Valid PluginRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(pluginService.create(create))
                .build();
    }

    @GET
    public Response search(@BeanParam PluginRecord.Query query) {
        return Response.ok(pluginService.search(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return pluginService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/code/{code}")
    public Response findByCode(@PathParam("code") String code) {
        return pluginService.findByCode(code)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PluginRecord.Update update) {
        return Response.ok(pluginService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        pluginService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/publish")
    public Response publish(@PathParam("id") UUID id) {
        return Response.ok(pluginService.publish(id)).build();
    }

    @POST
    @Path("/{id}/suspend")
    public Response suspend(@PathParam("id") UUID id, @QueryParam("reason") String reason) {
        return Response.ok(pluginService.suspend(id, reason)).build();
    }

    @GET
    @Path("/summary")
    public Response getSummary() {
        return Response.ok(pluginService.getSummary()).build();
    }

    @GET
    @Path("/developer/{developerId}")
    public Response findByDeveloper(@PathParam("developerId") UUID developerId) {
        return Response.ok(pluginService.findByDeveloperId(developerId)).build();
    }

    @GET
    @Path("/category/{categoryId}")
    public Response findByCategory(@PathParam("categoryId") UUID categoryId) {
        return Response.ok(pluginService.findByCategoryId(categoryId)).build();
    }

    // Version APIs
    @POST
    @Path("/{pluginId}/versions")
    public Response createVersion(
            @PathParam("pluginId") UUID pluginId,
            @Valid PluginVersionRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(pluginService.createVersion(pluginId, create))
                .build();
    }

    @GET
    @Path("/{pluginId}/versions")
    public Response listVersions(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(pluginService.findVersionsByPluginId(pluginId)).build();
    }

    @GET
    @Path("/{pluginId}/versions/latest")
    public Response getLatestVersion(@PathParam("pluginId") UUID pluginId) {
        return pluginService.findLatestVersion(pluginId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/versions/{versionId}")
    public Response getVersionById(@PathParam("versionId") UUID versionId) {
        return pluginService.findVersionById(versionId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/versions/{versionId}")
    public Response updateVersion(
            @PathParam("versionId") UUID versionId,
            @Valid PluginVersionRecord.Create update) {
        return Response.ok(pluginService.updateVersion(versionId, update)).build();
    }

    @DELETE
    @Path("/versions/{versionId}")
    public Response deleteVersion(@PathParam("versionId") UUID versionId) {
        pluginService.deleteVersion(versionId);
        return Response.noContent().build();
    }

    @POST
    @Path("/versions/{versionId}/publish")
    public Response publishVersion(@PathParam("versionId") UUID versionId) {
        return Response.ok(pluginService.publishVersion(versionId)).build();
    }
}