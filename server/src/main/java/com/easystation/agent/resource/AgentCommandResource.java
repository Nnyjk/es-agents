package com.easystation.agent.resource;

import com.easystation.agent.record.AgentCommandRecord;
import com.easystation.agent.service.AgentCommandService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/agents/commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentCommandResource {

    @Inject
    AgentCommandService agentCommandService;

    @GET
    public List<AgentCommandRecord> list(@QueryParam("templateId") UUID templateId) {
        return agentCommandService.list(templateId);
    }

    @GET
    @Path("/{id}")
    public AgentCommandRecord get(@PathParam("id") UUID id) {
        return agentCommandService.get(id);
    }

    @POST
    public Response create(@Valid AgentCommandRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentCommandService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public AgentCommandRecord update(@PathParam("id") UUID id, @Valid AgentCommandRecord.Update dto) {
        return agentCommandService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentCommandService.delete(id);
        return Response.noContent().build();
    }
}
