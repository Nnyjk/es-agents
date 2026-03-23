package com.easystation.plugin.resource;

import com.easystation.plugin.dto.PluginInstallationRecord;
import com.easystation.plugin.service.PluginInstallationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/plugin-installations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginInstallationResource {

    @Inject
    PluginInstallationService installationService;

    @POST
    public Response install(@Valid PluginInstallationRecord.Install install) {
        return Response.status(Response.Status.CREATED)
                .entity(installationService.install(install))
                .build();
    }

    @POST
    @Path("/batch")
    public Response batchInstall(@Valid List<PluginInstallationRecord.Install> installs) {
        return Response.status(Response.Status.CREATED)
                .entity(installationService.batchInstall(installs))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return installationService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/agent/{agentId}")
    public Response findByAgentId(@PathParam("agentId") UUID agentId) {
        return Response.ok(installationService.findByAgentId(agentId)).build();
    }

    @GET
    @Path("/plugin/{pluginId}")
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(installationService.findByPluginId(pluginId)).build();
    }

    @PUT
    @Path("/{id}/enable")
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(installationService.enable(id)).build();
    }

    @PUT
    @Path("/{id}/disable")
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(installationService.disable(id)).build();
    }

    @PUT
    @Path("/{id}/upgrade")
    public Response upgrade(
            @PathParam("id") UUID id,
            @QueryParam("versionId") UUID versionId) {
        return Response.ok(installationService.upgrade(id, versionId)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response uninstall(@PathParam("id") UUID id) {
        installationService.uninstall(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/status")
    public Response getStatus(@PathParam("id") UUID id) {
        return Response.ok(installationService.getStatus(id)).build();
    }

    @GET
    @Path("/{id}/logs")
    public Response getLogs(
            @PathParam("id") UUID id,
            @QueryParam("lines") @DefaultValue("100") int lines) {
        return Response.ok(installationService.getLogs(id, lines)).build();
    }

    @GET
    @Path("/{id}/metrics")
    public Response getMetrics(@PathParam("id") UUID id) {
        return Response.ok(installationService.getMetrics(id)).build();
    }
}