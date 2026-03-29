package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.CommandCategory;
import com.easystation.agent.record.CommandExecutionRecord;
import com.easystation.agent.record.CommandTemplateRecord;
import com.easystation.agent.service.CommandTemplateService;
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
import java.util.UUID;

@Path("/commands/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandTemplateResource {

    @Inject
    CommandTemplateService commandTemplateService;

    /**
     * List all command templates.
     * GET /commands/templates?category={category}&activeOnly={true/false}
     */
    @GET
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<CommandTemplateRecord.ListResponse> list(
            @QueryParam("category") CommandCategory category,
            @QueryParam("activeOnly") Boolean activeOnly) {
        return commandTemplateService.list(category, activeOnly);
    }

    /**
     * Get command template by ID.
     * GET /commands/templates/{id}
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public CommandTemplateRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return commandTemplateService.getById(id);
    }

    /**
     * Create a new command template.
     * POST /commands/templates
     */
    @POST
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:create")
    public Response create(
            @Valid CommandTemplateRecord.CreateRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        CommandTemplateRecord.DetailResponse response = commandTemplateService.create(request, username);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Update a command template.
     * PUT /commands/templates/{id}
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:edit")
    public CommandTemplateRecord.DetailResponse update(
            @PathParam("id") UUID id,
            @Valid CommandTemplateRecord.UpdateRequest request) {
        return commandTemplateService.update(id, request);
    }

    /**
     * Delete a command template.
     * DELETE /commands/templates/{id}
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"Admin"})
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        commandTemplateService.delete(id);
        return Response.noContent().build();
    }

    /**
     * Execute a command template on an agent instance.
     * POST /commands/templates/{id}/execute
     */
    @POST
    @Path("/{id}/execute")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public CommandTemplateRecord.ExecuteResponse execute(
            @PathParam("id") UUID id,
            @Valid CommandTemplateRecord.ExecuteRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return commandTemplateService.execute(id, request, username);
    }

    /**
     * Get execution history for a template.
     * GET /commands/templates/{id}/executions
     */
    @GET
    @Path("/{id}/executions")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<CommandExecutionRecord.ListResponse> getExecutionHistory(@PathParam("id") UUID id) {
        return commandTemplateService.getExecutionHistory(id);
    }

    /**
     * Get execution detail by ID.
     * GET /commands/executions/{executionId}
     */
    @GET
    @Path("/executions/{executionId}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getExecutionById(@PathParam("executionId") UUID executionId) {
        return commandTemplateService.getExecutionById(executionId);
    }
}