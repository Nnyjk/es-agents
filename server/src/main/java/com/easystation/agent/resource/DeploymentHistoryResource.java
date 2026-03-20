package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.DeploymentStatus;
import com.easystation.agent.record.DeploymentHistoryRecord;
import com.easystation.agent.service.DeploymentHistoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/deployments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeploymentHistoryResource {

    @Inject
    DeploymentHistoryService deploymentHistoryService;

    /**
     * Get all deployment history for an agent instance.
     * GET /deployments/history?agentInstanceId={uuid}
     */
    @GET
    @Path("/history")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    public List<DeploymentHistoryRecord.ListResponse> getHistory(
            @QueryParam("agentInstanceId") UUID agentInstanceId) {
        if (agentInstanceId == null) {
            throw new WebApplicationException("agentInstanceId is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.getHistoryByAgentInstance(agentInstanceId);
    }

    /**
     * Get deployment history detail by ID.
     * GET /deployments/history/{id}
     */
    @GET
    @Path("/history/{id}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    public DeploymentHistoryRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return deploymentHistoryService.getById(id);
    }

    /**
     * Create a new deployment history record.
     * POST /deployments/history
     */
    @POST
    @Path("/history")
    @RolesAllowed({"Admin", "Ops"})
    public Response create(
            DeploymentHistoryRecord.CreateRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        DeploymentHistoryRecord.DetailResponse response = deploymentHistoryService.create(request, username);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Update deployment status.
     * PUT /deployments/history/{id}/status
     */
    @PUT
    @Path("/history/{id}/status")
    @RolesAllowed({"Admin", "Ops"})
    public DeploymentHistoryRecord.DetailResponse updateStatus(
            @PathParam("id") UUID id,
            @QueryParam("status") DeploymentStatus status) {
        if (status == null) {
            throw new WebApplicationException("status is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.updateStatus(id, status);
    }

    /**
     * Rollback to a specific deployment.
     * POST /deployments/history/{id}/rollback
     */
    @POST
    @Path("/history/{id}/rollback")
    @RolesAllowed({"Admin", "Ops"})
    public DeploymentHistoryRecord.RollbackResponse rollback(
            @PathParam("id") UUID id,
            DeploymentHistoryRecord.RollbackRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return deploymentHistoryService.rollback(id, request != null ? request.reason() : null, username);
    }

    /**
     * Delete deployment history.
     * DELETE /deployments/history/{id}
     */
    @DELETE
    @Path("/history/{id}")
    @RolesAllowed({"Admin"})
    public Response delete(@PathParam("id") UUID id) {
        deploymentHistoryService.delete(id);
        return Response.noContent().build();
    }

    /**
     * Get the latest successful deployment for an agent instance.
     * GET /deployments/latest?agentInstanceId={uuid}
     */
    @GET
    @Path("/latest")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    public DeploymentHistoryRecord.DetailResponse getLatestSuccessful(
            @QueryParam("agentInstanceId") UUID agentInstanceId) {
        if (agentInstanceId == null) {
            throw new WebApplicationException("agentInstanceId is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.getLatestSuccessful(agentInstanceId);
    }
}