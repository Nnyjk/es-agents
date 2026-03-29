package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ApplicationStatus;
import com.easystation.deployment.service.ApplicationService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/api/deployment/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    ApplicationService applicationService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<ApplicationDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("name") String name,
            @QueryParam("project") String project,
            @QueryParam("owner") String owner,
            @QueryParam("status") ApplicationStatus status) {
        return applicationService.listApplications(pageNum, pageSize, name, project, owner, status);
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("deployment:view")
    public ApplicationDTO get(@PathParam("id") UUID id) {
        return applicationService.getApplication(id);
    }

    @POST
    @RequiresPermission("deployment:create")
    public ApplicationDTO create(ApplicationDTO dto) {
        ApplicationDTO created = applicationService.createApplication(dto);
        recordAuditLog(AuditAction.CREATE_APPLICATION, AuditResult.SUCCESS,
                "创建应用：" + created.name, "Application", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("deployment:edit")
    public ApplicationDTO update(@PathParam("id") UUID id, ApplicationDTO dto) {
        ApplicationDTO updated = applicationService.updateApplication(id, dto);
        recordAuditLog(AuditAction.UPDATE_APPLICATION, AuditResult.SUCCESS,
                "更新应用：" + updated.name, "Application", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("deployment:delete")
    public Response delete(@PathParam("id") UUID id) {
        ApplicationDTO dto = applicationService.getApplication(id);
        applicationService.deleteApplication(id);
        recordAuditLog(AuditAction.DELETE_APPLICATION, AuditResult.SUCCESS,
                "删除应用：" + (dto != null ? dto.name : id), "Application", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/archive")
    @RequiresPermission("deployment:edit")
    public ApplicationDTO archive(@PathParam("id") UUID id) {
        ApplicationDTO archived = applicationService.archiveApplication(id);
        recordAuditLog(AuditAction.ARCHIVE_APPLICATION, AuditResult.SUCCESS,
                "归档应用：" + archived.name, "Application", id);
        return archived;
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}