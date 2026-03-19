package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentLogRecord;
import com.easystation.agent.service.AgentLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentLogResource {

    @Inject
    AgentLogService agentLogService;

    @GET
    @Path("/{agentId}")
    public Response queryLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("level") String level,
            @QueryParam("keyword") String keyword,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        var query = new AgentLogRecord.Query(
            agentId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            level,
            keyword,
            null, // TODO: parse startTime
            null  // TODO: parse endTime
        );
        
        var result = agentLogService.queryLogs(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{agentId}/stats")
    public Response getLogStats(@PathParam("agentId") UUID agentId) {
        var stats = agentLogService.getLogStats(agentId);
        return Response.ok(stats).build();
    }

    @GET
    @Path("/{agentId}/tail")
    public Response tailLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("lines") Integer lines) {
        
        var logs = agentLogService.getLatestLogs(agentId, lines != null ? lines : 50);
        return Response.ok(logs).build();
    }
}