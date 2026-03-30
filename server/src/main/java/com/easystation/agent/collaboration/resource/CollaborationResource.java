package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * 协作会话 REST API
 */
@Path("/api/agent/collaboration/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollaborationResource {

    @Inject
    CollaborationService collaborationService;

    /**
     * 创建协作会话
     */
    @POST
    public Response createSession(CreateSessionRequest request, @HeaderParam("X-Agent-ID") String agentId) {
        if (request.name == null || request.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: name is required\"}")
                    .build();
        }

        // Set creator agent ID from header if not provided
        if (request.creatorAgentId == null || request.creatorAgentId.trim().isEmpty()) {
            request.creatorAgentId = agentId != null ? agentId : "system";
        }

        CollaborationSessionDTO session = collaborationService.createSession(request);
        return Response.status(Response.Status.CREATED).entity(session).build();
    }

    /**
     * 获取会话详情
     */
    @GET
    @Path("/{sessionId}")
    public Response getSession(@PathParam("sessionId") Long sessionId) {
        CollaborationSessionDTO session = collaborationService.getSession(sessionId);
        if (session == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Session not found: " + sessionId + "\"}")
                    .build();
        }
        return Response.ok(session).build();
    }

    /**
     * 列出所有会话
     */
    @GET
    @Path("/all")
    public Response listAllSessions() {
        List<CollaborationSessionDTO> sessions = collaborationService.listSessions();
        return Response.ok(sessions).build();
    }

    /**
     * 列出活跃会话
     */
    @GET
    public Response listActiveSessions() {
        List<CollaborationSessionDTO> sessions = collaborationService.getActiveSessions();
        return Response.ok(sessions).build();
    }

    /**
     * 加入会话
     */
    @POST
    @Path("/{sessionId}/join")
    public Response joinSession(@PathParam("sessionId") Long sessionId,
                               @HeaderParam("X-Agent-ID") String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Agent-ID header is required\"}")
                    .build();
        }
        CollaborationSessionDTO session = collaborationService.joinSession(sessionId, agentId);
        return Response.ok(session).build();
    }

    /**
     * 离开会话
     */
    @POST
    @Path("/{sessionId}/leave")
    public Response leaveSession(@PathParam("sessionId") Long sessionId,
                                @HeaderParam("X-Agent-ID") String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Agent-ID header is required\"}")
                    .build();
        }
        CollaborationSessionDTO session = collaborationService.leaveSession(sessionId, agentId);
        return Response.ok(session).build();
    }

    /**
     * 关闭会话
     */
    @POST
    @Path("/{sessionId}/close")
    public Response closeSession(@PathParam("sessionId") Long sessionId) {
        CollaborationSessionDTO session = collaborationService.closeSession(sessionId);
        return Response.ok(session).build();
    }
}