package com.easystation.agent.resource;

import com.easystation.agent.record.AgentTemplateRecord;
import com.easystation.agent.service.AgentTemplateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentTemplateResource {

    @Inject
    AgentTemplateService agentTemplateService;

    @GET
    public Response list(
        @QueryParam("osType") String osType,
        @QueryParam("sourceType") String sourceType
    ) {
        return Response.ok(agentTemplateService.list(osType, sourceType)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentTemplateService.get(id)).build();
    }

    @POST
    public Response create(@Valid AgentTemplateRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentTemplateService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentTemplateRecord.Update dto) {
        return Response.ok(agentTemplateService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentTemplateService.delete(id);
        return Response.noContent().build();
    }
}
