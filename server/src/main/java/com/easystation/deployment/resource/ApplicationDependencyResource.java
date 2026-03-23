package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ApplicationDependency;
import com.easystation.deployment.dto.ApplicationDependencyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ApplicationDependencyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * 应用依赖管理 API
 */
@Path("/api/deployment/applications/{applicationId}/dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationDependencyResource {

    @Inject
    ApplicationDependencyService dependencyService;

    @GET
    public PageResultDTO<ApplicationDependencyDTO> list(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("type") ApplicationDependency.DependencyType type,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return dependencyService.listDependencies(pageNum, pageSize, applicationId, environmentId, type);
    }

    @GET
    @Path("/all")
    public List<ApplicationDependencyDTO> listAll(@PathParam("applicationId") UUID applicationId) {
        return dependencyService.getByApplicationId(applicationId);
    }

    @GET
    @Path("/{id}")
    public ApplicationDependencyDTO get(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        ApplicationDependencyDTO dto = dependencyService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            ApplicationDependencyDTO dto) {
        dto.applicationId = applicationId;
        ApplicationDependencyDTO created = dependencyService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public ApplicationDependencyDTO update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id,
            ApplicationDependencyDTO dto) {
        ApplicationDependencyDTO updated = dependencyService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        return updated;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        boolean deleted = dependencyService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteByApplication(@PathParam("applicationId") UUID applicationId) {
        int count = dependencyService.deleteByApplication(applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
    }
}