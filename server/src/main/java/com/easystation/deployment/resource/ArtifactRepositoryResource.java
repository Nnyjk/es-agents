package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ArtifactRepository;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.ArtifactRepositoryDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ArtifactRepositoryService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

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

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

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
            recordAuditLog(AuditAction.CREATE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                    "创建制品仓库：" + created.name, "ArtifactRepository", created.id);
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
            recordAuditLog(AuditAction.UPDATE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                    "更新制品仓库：" + updated.name, "ArtifactRepository", id);
            return updated;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.getById(id);
        boolean deleted = repositoryService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                "删除制品仓库：" + (dto != null ? dto.name : id), "ArtifactRepository", id);
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
        recordAuditLog(AuditAction.ACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "激活制品仓库：" + dto.name, "ArtifactRepository", id);
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
        recordAuditLog(AuditAction.DEACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "停用制品仓库：" + dto.name, "ArtifactRepository", id);
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
        recordAuditLog(AuditAction.SET_DEFAULT_RESOURCE, AuditResult.SUCCESS,
                "设置默认制品仓库：" + dto.name, "ArtifactRepository", id);
        return dto;
    }

    private void recordAuditLog(AuditAction action, AuditResult result,
                               String description, String resourceType, UUID resourceId) {
        try {
            String username = securityContext != null && securityContext.getUserPrincipal() != null
                    ? securityContext.getUserPrincipal().getName() : "system";
            String clientIp = httpHeaders != null
                    ? httpHeaders.getHeaderString("X-Forwarded-For")
                    : null;
            if (clientIp == null && httpHeaders != null) {
                clientIp = httpHeaders.getHeaderString("X-Real-IP");
            }
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/repositories");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}