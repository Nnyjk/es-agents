package com.easystation.agent.resource;

import com.easystation.agent.record.AgentCredentialRecord;
import com.easystation.agent.service.AgentCredentialService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/credentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentCredentialResource {

    @Inject
    AgentCredentialService agentCredentialService;

    @GET
    public Response list() {
        return Response.ok(agentCredentialService.list()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentCredentialService.get(id)).build();
    }

    @POST
    public Response create(@Valid AgentCredentialRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
            .entity(agentCredentialService.create(dto))
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentCredentialRecord.Update dto) {
        return Response.ok(agentCredentialService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentCredentialService.delete(id);
        return Response.noContent().build();
    }
}
