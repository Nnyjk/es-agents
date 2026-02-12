package com.easystation.agent.resource;

import com.easystation.agent.record.AgentRepositoryRecord;
import com.easystation.agent.service.AgentRepositoryService;
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
    public Response list() {
        return Response.ok(agentRepositoryService.list()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentRepositoryService.get(id)).build();
    }

    @POST
    public Response create(@Valid AgentRepositoryRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
            .entity(agentRepositoryService.create(dto))
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentRepositoryRecord.Update dto) {
        return Response.ok(agentRepositoryService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentRepositoryService.delete(id);
        return Response.noContent().build();
    }
}
