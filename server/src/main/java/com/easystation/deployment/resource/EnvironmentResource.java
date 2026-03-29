package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.EnvironmentType;
import com.easystation.deployment.service.EnvironmentService;
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

@Path("/api/deployment/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

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
        EnvironmentDTO created = environmentService.createEnvironment(dto);
        recordAuditLog(AuditAction.CREATE_ENVIRONMENT, AuditResult.SUCCESS,
                "创建环境：" + created.name, "Environment", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("environment:edit")
    public EnvironmentDTO update(@PathParam("id") UUID id, EnvironmentDTO dto) {
        EnvironmentDTO updated = environmentService.updateEnvironment(id, dto);
        recordAuditLog(AuditAction.UPDATE_ENVIRONMENT, AuditResult.SUCCESS,
                "更新环境：" + updated.name, "Environment", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("environment:delete")
    public Response delete(@PathParam("id") UUID id) {
        EnvironmentDTO dto = environmentService.getEnvironment(id);
        environmentService.deleteEnvironment(id);
        recordAuditLog(AuditAction.DELETE_ENVIRONMENT, AuditResult.SUCCESS,
                "删除环境：" + (dto != null ? dto.name : id), "Environment", id);
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/environments");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}