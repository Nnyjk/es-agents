package com.easystation.agent.resource;

import com.easystation.agent.record.AgentInstanceRecord;
import com.easystation.agent.service.AgentInstanceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentInstanceResource {

    @Inject
    AgentInstanceService agentInstanceService;

    @GET
    public Response list(@QueryParam("hostId") UUID hostId) {
        return Response.ok(agentInstanceService.list(hostId)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.get(id)).build();
    }

    @POST
    public Response create(@Valid AgentInstanceRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentInstanceService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentInstanceRecord.Update dto) {
        return Response.ok(agentInstanceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentInstanceService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/commands")
    public Response executeCommand(@PathParam("id") UUID id, @Valid AgentInstanceRecord.ExecuteCommand dto) {
        agentInstanceService.executeCommand(id, dto);
        return Response.accepted().build();
    }
}
