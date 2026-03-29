package com.easystation.deployment.resource;

import com.easystation.deployment.dto.*;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.service.ReleaseService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReleaseResource {

    @Inject
    ReleaseService releaseService;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<ReleaseDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("releaseId") String releaseId,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("status") ReleaseStatus status) {
        return releaseService.listReleases(pageNum, pageSize, releaseId, applicationId, environmentId, status);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public ReleaseDTO get(@PathParam("id") UUID id) {
        return releaseService.getRelease(id);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}/detail")
    public ReleaseDTO getDetail(@PathParam("id") UUID id) {
        return releaseService.getReleaseDetail(id);
    }

    @POST
    @RequiresPermission("deployment:create")
    public ReleaseDTO create(ReleaseDTO dto) {
        return releaseService.createRelease(dto);
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/submit")
    public ReleaseDTO submitForApproval(@PathParam("id") UUID id,
                                        @QueryParam("submittedBy") @DefaultValue("system") String submittedBy) {
        return releaseService.submitForApproval(id, submittedBy);
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/approve")
    public ReleaseDTO approve(@PathParam("id") UUID id,
                              @QueryParam("approvedBy") @DefaultValue("system") String approvedBy) {
        return releaseService.approveRelease(id, approvedBy);
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/reject")
    public ReleaseDTO reject(@PathParam("id") UUID id,
                             @QueryParam("rejectedBy") @DefaultValue("system") String rejectedBy,
                             @QueryParam("reason") String reason) {
        return releaseService.rejectRelease(id, rejectedBy, reason);
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/start")
    public ReleaseDTO start(@PathParam("id") UUID id,
                            @QueryParam("deployedBy") @DefaultValue("system") String deployedBy) {
        return releaseService.startRelease(id, deployedBy);
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/rollback")
    public ReleaseDTO rollback(@PathParam("id") UUID id,
                               @QueryParam("rolledBackBy") @DefaultValue("system") String rolledBackBy) {
        return releaseService.rollbackRelease(id, rolledBackBy);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/history/{applicationId}")
    public List<ReleaseDTO> getHistory(@PathParam("applicationId") UUID applicationId) {
        return releaseService.getReleaseHistory(applicationId);
    }
}