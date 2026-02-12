package com.easystation.agent.resource;

import com.easystation.agent.record.AgentSourceRecord;
import com.easystation.agent.service.AgentSourceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.UUID;

@Path("/agents/sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentSourceResource {

    @Inject
    AgentSourceService agentSourceService;

    @GET
    public Response list() {
        return Response.ok(agentSourceService.list()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentSourceService.get(id)).build();
    }

    @POST
    public Response create(@Valid AgentSourceRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentSourceService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentSourceRecord.Update dto) {
        return Response.ok(agentSourceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentSourceService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("id") UUID id) {
        String[] fileName = new String[1];
        InputStream is = agentSourceService.getSourceStream(id, fileName);
        
        return Response.ok(is)
                .header("Content-Disposition", "attachment; filename=\"" + fileName[0] + "\"")
                .build();
    }
}
