package com.easystation.agent.resource;

import com.easystation.agent.record.HeartbeatRequest;
import com.easystation.agent.service.AgentInstanceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/gateway")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentGatewayResource {

    @Inject
    AgentInstanceService agentInstanceService;

    @POST
    @Path("/heartbeat")
    public Response heartbeat(@HeaderParam("X-Agent-Secret") String secretKey, HeartbeatRequest request) {
        agentInstanceService.handleHeartbeat(request, secretKey);
        return Response.ok().build();
    }

    @GET
    @Path("/commands")
    public Response fetchCommands(@HeaderParam("X-Agent-Secret") String secretKey, @QueryParam("agentId") String agentId) {
        return Response.ok(agentInstanceService.fetchPendingTasks(agentId, secretKey)).build();
    }
}
