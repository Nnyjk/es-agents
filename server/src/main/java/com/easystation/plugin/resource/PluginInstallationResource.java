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
    public Response install(@Valid PluginInstallationRecord.Install install, @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(installationService.install(install, userId))
                .build();
    }

    @POST
    @Path("/batch")
    public Response batchInstall(@Valid PluginInstallationRecord.BatchInstall batchInstall, @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(installationService.batchInstall(batchInstall, userId))
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
    @Path("/{id}/config")
    public Response updateConfig(@PathParam("id") UUID id, @Valid PluginInstallationRecord.UpdateConfig update) {
        return Response.ok(installationService.updateConfig(id, update)).build();
    }

    @PUT
    @Path("/{id}/start")
    public Response start(@PathParam("id") UUID id) {
        return Response.ok(installationService.start(id)).build();
    }

    @PUT
    @Path("/{id}/stop")
    public Response stop(@PathParam("id") UUID id) {
        return Response.ok(installationService.stop(id)).build();
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

    @DELETE
    @Path("/{id}")
    public Response uninstall(@PathParam("id") UUID id) {
        installationService.uninstall(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/search")
    public Response search(@BeanParam PluginInstallationRecord.Query query) {
        return Response.ok(installationService.search(query)).build();
    }

    private UUID getUserId(SecurityContext securityContext) {
        String userIdStr = securityContext.getUserPrincipal().getName();
        return UUID.fromString(userIdStr);
    }
}