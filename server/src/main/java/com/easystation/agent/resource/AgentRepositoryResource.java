package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentRepositoryRecord;
import com.easystation.agent.service.AgentRepositoryService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/repositories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentRepositoryResource {

    @Inject
    AgentRepositoryService agentRepositoryService;

    @GET
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(agentRepositoryService.list()).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentRepositoryService.get(id)).build();
    }

    @POST
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentRepositoryRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
            .entity(agentRepositoryService.create(dto))
            .build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentRepositoryRecord.Update dto) {
        return Response.ok(agentRepositoryService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentRepositoryService.delete(id);
        return Response.noContent().build();
    }
}
