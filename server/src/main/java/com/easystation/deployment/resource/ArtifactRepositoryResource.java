package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ArtifactRepository;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.dto.ArtifactRepositoryDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ArtifactRepositoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * 制品仓库管理 API
 */
@Path("/api/deployment/repositories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtifactRepositoryResource {

    @Inject
    ArtifactRepositoryService repositoryService;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<ArtifactRepositoryDTO> list(
            @QueryParam("type") ArtifactRepository.RepositoryType type,
            @QueryParam("active") Boolean active,
            @QueryParam("keyword") String keyword,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return repositoryService.listRepositories(pageNum, pageSize, type, active, keyword);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/type/{type}")
    public List<ArtifactRepositoryDTO> listByType(@PathParam("type") ArtifactRepository.RepositoryType type) {
        return repositoryService.getByType(type);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/default/{type}")
    public ArtifactRepositoryDTO getDefault(@PathParam("type") ArtifactRepository.RepositoryType type) {
        ArtifactRepositoryDTO dto = repositoryService.getDefaultRepository(type);
        if (dto == null) {
            throw new WebApplicationException("No default repository found for type: " + type, Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public ArtifactRepositoryDTO get(@PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/name/{name}")
    public ArtifactRepositoryDTO getByName(@PathParam("name") String name) {
        ArtifactRepositoryDTO dto = repositoryService.getByName(name);
        if (dto == null) {
            throw new WebApplicationException("Repository not found: " + name, Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    public Response create(ArtifactRepositoryDTO dto) {
        try {
            ArtifactRepositoryDTO created = repositoryService.create(dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @PUT
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public ArtifactRepositoryDTO update(@PathParam("id") UUID id, ArtifactRepositoryDTO dto) {
        try {
            ArtifactRepositoryDTO updated = repositoryService.update(id, dto);
            if (updated == null) {
                throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
            }
            return updated;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        boolean deleted = repositoryService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return Response.noContent().build();
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/activate")
    public ArtifactRepositoryDTO activate(@PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setActive(id, true);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/deactivate")
    public ArtifactRepositoryDTO deactivate(@PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setActive(id, false);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/default")
    public ArtifactRepositoryDTO setDefault(@PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setDefault(id);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }
}