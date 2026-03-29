package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.EnvironmentType;
import com.easystation.deployment.service.EnvironmentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @GET
    @RequiresPermission("environment:view")
    public PageResultDTO<EnvironmentDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("name") String name,
            @QueryParam("environmentType") EnvironmentType environmentType,
            @QueryParam("active") Boolean active) {
        return environmentService.listEnvironments(pageNum, pageSize, name, environmentType, active);
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("environment:view")
    public EnvironmentDTO get(@PathParam("id") UUID id) {
        return environmentService.getEnvironment(id);
    }

    @POST
    @RequiresPermission("environment:create")
    public EnvironmentDTO create(EnvironmentDTO dto) {
        return environmentService.createEnvironment(dto);
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("environment:edit")
    public EnvironmentDTO update(@PathParam("id") UUID id, EnvironmentDTO dto) {
        return environmentService.updateEnvironment(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("environment:delete")
    public Response delete(@PathParam("id") UUID id) {
        environmentService.deleteEnvironment(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/resources")
    @RequiresPermission("environment:view")
    public List<EnvironmentResourceDTO> getResources(@PathParam("id") UUID id) {
        return environmentService.getEnvironmentResources(id);
    }

    @GET
    @Path("/{id}/applications")
    @RequiresPermission("environment:view")
    public List<EnvironmentApplicationDTO> getApplications(@PathParam("id") UUID id) {
        return environmentService.getEnvironmentApplications(id);
    }
}