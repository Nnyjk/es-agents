package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.domain.CollaborationSession;
import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

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
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: name is required\"}")
                    .build();
        }

        CollaborationSession session = collaborationService.createSession(
                request.name,
                request.description,
                agentId != null ? agentId : "system",
                request.agentIds
        );

        return Response.status(Response.Status.CREATED)
                .entity(CollaborationSessionDTO.fromEntity(session))
                .build();
    }

    /**
     * 获取会话详情
     */
    @GET
    @Path("/{sessionId}")
    public Response getSession(@PathParam("sessionId") Long sessionId) {
        CollaborationSession session = collaborationService.getSession(sessionId);
        if (session == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Session not found: " + sessionId + "\"}")
                    .build();
        }
        return Response.ok(CollaborationSessionDTO.fromEntity(session)).build();
    }

    /**
     * 列出活跃会话
     */
    @GET
    public Response listActiveSessions() {
        List<CollaborationSessionDTO> sessions = collaborationService.listActiveSessions()
                .stream()
                .map(CollaborationSessionDTO::fromEntity)
                .collect(Collectors.toList());
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
        collaborationService.joinSession(sessionId, agentId);
        return Response.noContent().build();
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
        collaborationService.leaveSession(sessionId, agentId);
        return Response.noContent().build();
    }

    /**
     * 关闭会话
     */
    @POST
    @Path("/{sessionId}/close")
    public Response closeSession(@PathParam("sessionId") Long sessionId) {
        collaborationService.closeSession(sessionId);
        return Response.noContent().build();
    }
}
