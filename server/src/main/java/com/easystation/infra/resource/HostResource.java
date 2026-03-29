package com.easystation.infra.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.infra.record.HostRecord;
import com.easystation.infra.service.HostService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.UUID;

@Path("/api/v1/hosts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HostResource {

    @Inject
    HostService hostService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @RequiresPermission("host:view")
    public Response list(@QueryParam("environmentId") UUID environmentId) {
        return Response.ok(hostService.list(environmentId)).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("host:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(hostService.get(id)).build();
    }

    @POST
    @RequiresPermission("host:create")
    public Response create(@Valid HostRecord.Create dto) {
        HostRecord.Detail created = hostService.create(dto);
        recordAuditLog(AuditAction.CREATE_HOST, AuditResult.SUCCESS,
                "创建主机：" + created.name(), "Host", created.id());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("host:edit")
    public Response update(@PathParam("id") UUID id, @Valid HostRecord.Update dto) {
        HostRecord.Detail updated = hostService.update(id, dto);
        recordAuditLog(AuditAction.UPDATE_HOST, AuditResult.SUCCESS,
                "更新主机：" + updated.name(), "Host", id);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("host:delete")
    public Response delete(@PathParam("id") UUID id) {
        HostRecord.Detail host = hostService.get(id);
        hostService.delete(id);
        recordAuditLog(AuditAction.DELETE_HOST, AuditResult.SUCCESS,
                "删除主机：" + (host != null ? host.name() : id), "Host", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/connect")
    @RequiresPermission("host:manage")
    public Response connect(@PathParam("id") UUID id) {
        hostService.connect(id);
        recordAuditLog(AuditAction.START_AGENT, AuditResult.SUCCESS,
                "连接主机", "Host", id);
        return Response.ok().build();
    }

    /**
     * Check host reachability - verifies if the host is accessible via TCP
     */
    @POST
    @Path("/{id}/check-reachability")
    @RequiresPermission("host:view")
    public Response checkReachability(@PathParam("id") UUID id) {
        return Response.ok(hostService.checkReachability(id)).build();
    }

    /**
     * Check reachability of all hosts
     */
    @POST
    @Path("/check-reachability")
    @RequiresPermission("host:view")
    public Response checkReachabilityAll() {
        return Response.ok(hostService.checkReachabilityAll()).build();
    }

    @GET
    @Path("/{id}/install-guide")
    @RequiresPermission("host:view")
    public Response getInstallGuide(@PathParam("id") UUID id) {
        return Response.ok(hostService.getInstallGuide(id)).build();
    }

    @GET
    @Path("/{id}/package")
    @RequiresPermission("host:view")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@PathParam("id") UUID id, @QueryParam("sourceId") UUID sourceId) {
        if (sourceId == null) {
            throw new WebApplicationException("sourceId is required", Response.Status.BAD_REQUEST);
        }

        StreamingOutput stream = hostService.downloadPackage(id, sourceId);
        String packageFileName = hostService.getPackageFileName(id);

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"" + packageFileName + "\"")
                .build();
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/v1/hosts");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}
