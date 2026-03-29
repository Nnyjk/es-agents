package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.agent.record.TaskRecord;
import com.easystation.agent.service.AgentTaskService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/agents/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentTaskResource {

    @Inject
    AgentTaskService agentTaskService;

    /**
     * Execute a command on an agent instance.
     * POST /agents/tasks/execute
     */
    @POST
    @Path("/execute")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public Response executeCommand(
            @Valid TaskRecord.ExecuteRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        AgentTaskRecord task = agentTaskService.execute(
                request.agentInstanceId(),
                request.commandId(),
                request.args(),
                username
        );
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    /**
     * Execute a script directly on an agent instance.
     * POST /agents/tasks/execute-script
     */
    @POST
    @Path("/execute-script")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public Response executeScript(
            @Valid TaskRecord.ExecuteScriptRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        AgentTaskRecord task = agentTaskService.executeScript(
                request.agentInstanceId(),
                request.script(),
                request.timeout(),
                username
        );
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    /**
     * Get task by ID.
     * GET /agents/tasks/{id}
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public AgentTaskRecord getById(@PathParam("id") UUID id) {
        return agentTaskService.get(id);
    }

    /**
     * List tasks by agent instance.
     * GET /agents/tasks?agentInstanceId={uuid}&status={status}&limit={n}
     */
    @GET
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<AgentTaskRecord> list(
            @QueryParam("agentInstanceId") UUID agentInstanceId,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        if (agentInstanceId != null) {
            return agentTaskService.listByAgentInstance(agentInstanceId, status, limit);
        } else {
            return agentTaskService.listRecent(limit);
        }
    }

    /**
     * Retry a failed task.
     * POST /agents/tasks/{id}/retry
     */
    @POST
    @Path("/{id}/retry")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public AgentTaskRecord retry(
            @PathParam("id") UUID id,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return agentTaskService.retry(id, username);
    }

    /**
     * Cancel a pending task.
     * POST /agents/tasks/{id}/cancel
     */
    @POST
    @Path("/{id}/cancel")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public AgentTaskRecord cancel(@PathParam("id") UUID id) {
        return agentTaskService.cancel(id);
    }

    /**
     * Delete a task.
     * DELETE /agents/tasks/{id}
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"Admin"})
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentTaskService.delete(id);
        return Response.noContent().build();
    }

    /**
     * Get task counts by status.
     * GET /agents/tasks/counts
     */
    @GET
    @Path("/counts")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskRecord.TaskCounts getCounts() {
        Map<AgentTaskStatus, Long> counts = agentTaskService.countByStatus();
        return new TaskRecord.TaskCounts(
                counts.getOrDefault(AgentTaskStatus.PENDING, 0L),
                counts.getOrDefault(AgentTaskStatus.SENT, 0L),
                counts.getOrDefault(AgentTaskStatus.RUNNING, 0L),
                counts.getOrDefault(AgentTaskStatus.SUCCESS, 0L),
                counts.getOrDefault(AgentTaskStatus.FAILED, 0L)
        );
    }
}