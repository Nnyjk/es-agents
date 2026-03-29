package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ApplicationDependency;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.ApplicationDependencyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ApplicationDependencyService;
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
 * 应用依赖管理 API
 */
@Path("/api/deployment/applications/{applicationId}/dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationDependencyResource {

    @Inject
    ApplicationDependencyService dependencyService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<ApplicationDependencyDTO> list(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("type") ApplicationDependency.DependencyType type,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return dependencyService.listDependencies(pageNum, pageSize, applicationId, environmentId, type);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/all")
    public List<ApplicationDependencyDTO> listAll(@PathParam("applicationId") UUID applicationId) {
        return dependencyService.getByApplicationId(applicationId);
    }

    @GET
    @RequiresPermission("deployment:view")
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
    @RequiresPermission("deployment:create")
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            ApplicationDependencyDTO dto) {
        dto.applicationId = applicationId;
        ApplicationDependencyDTO created = dependencyService.create(dto);
        recordAuditLog(AuditAction.CREATE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "创建应用依赖", "ApplicationDependency", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public ApplicationDependencyDTO update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id,
            ApplicationDependencyDTO dto) {
        ApplicationDependencyDTO updated = dependencyService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.UPDATE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "更新应用依赖", "ApplicationDependency", id);
        return updated;
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        boolean deleted = dependencyService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "删除应用依赖", "ApplicationDependency", id);
        return Response.noContent().build();
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    public Response deleteByApplication(@PathParam("applicationId") UUID applicationId) {
        long count = dependencyService.deleteByApplication(applicationId);
        recordAuditLog(AuditAction.DELETE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "删除应用所有依赖，数量：" + count, "ApplicationDependency", applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications/dependencies");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}