package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.domain.AgentMessage;
import com.easystation.agent.collaboration.dto.AgentMessageDTO;
import com.easystation.agent.collaboration.dto.SendMessageRequest;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 消息 REST API
 */
@Path("/api/agent/collaboration/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    CollaborationService collaborationService;

    /**
     * 发送消息
     */
    @POST
    public Response sendMessage(@QueryParam("sessionId") Long sessionId,
                               SendMessageRequest request,
                               @HeaderParam("X-Agent-ID") String agentId) {
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: type, fromAgentId, and content are required\"}")
                    .build();
        }

        AgentMessage message = collaborationService.sendMessage(
                sessionId,
                request.type,
                request.fromAgentId != null ? request.fromAgentId : agentId,
                request.toAgentId,
                request.subject,
                request.content,
                request.correlationId
        );

        return Response.status(Response.Status.CREATED)
                .entity(AgentMessageDTO.fromEntity(message))
                .build();
    }

    /**
     * 获取消息历史
     */
    @GET
    public Response getMessages(@QueryParam("sessionId") Long sessionId,
                               @QueryParam("sinceMessageId") Long sinceMessageId,
                               @QueryParam("limit") @DefaultValue("50") int limit) {
        List<AgentMessage> messages = collaborationService.getMessages(sessionId, sinceMessageId, limit);
        List<AgentMessageDTO> dtos = messages.stream()
                .map(AgentMessageDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    /**
     * 获取未读消息
     */
    @GET
    @Path("/unread")
    public Response getUnreadMessages(@QueryParam("sessionId") Long sessionId,
                                     @QueryParam("agentId") String agentId) {
        List<AgentMessage> messages = collaborationService.getUnreadMessages(sessionId, agentId);
        List<AgentMessageDTO> dtos = messages.stream()
                .map(AgentMessageDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    /**
     * 标记消息为已读
     */
    @POST
    @Path("/{messageId}/read")
    public Response markAsRead(@PathParam("messageId") Long messageId) {
        collaborationService.markMessageAsRead(messageId);
        return Response.noContent().build();
    }
}
