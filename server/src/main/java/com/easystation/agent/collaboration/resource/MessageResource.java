package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.dto.AgentMessageDTO;
import com.easystation.agent.collaboration.dto.SendMessageRequest;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

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
    public Response sendMessage(SendMessageRequest request,
                               @HeaderParam("X-Agent-ID") String agentId) {
        // Validate required fields
        if (request.sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"sessionId is required\"}")
                    .build();
        }
        if (request.content == null || request.content.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"content is required\"}")
                    .build();
        }

        // Set from agent ID from header if not provided
        if (request.fromAgentId == null || request.fromAgentId.trim().isEmpty()) {
            request.fromAgentId = agentId != null ? agentId : "system";
        }

        AgentMessageDTO message = collaborationService.sendMessage(request);
        return Response.status(Response.Status.CREATED).entity(message).build();
    }

    /**
     * 获取会话消息
     */
    @GET
    @Path("/session/{sessionId}")
    public Response getSessionMessages(@PathParam("sessionId") Long sessionId) {
        List<AgentMessageDTO> messages = collaborationService.getSessionMessages(sessionId);
        return Response.ok(messages).build();
    }

    /**
     * 获取 Agent 的消息
     */
    @GET
    @Path("/agent/{agentId}")
    public Response getMessagesForAgent(@PathParam("agentId") String agentId) {
        List<AgentMessageDTO> messages = collaborationService.getMessagesForAgent(agentId);
        return Response.ok(messages).build();
    }

    /**
     * 获取消息详情
     */
    @GET
    @Path("/{messageId}")
    public Response getMessage(@PathParam("messageId") Long messageId) {
        AgentMessageDTO message = collaborationService.getMessageById(messageId);
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Message not found: " + messageId + "\"}")
                    .build();
        }
        return Response.ok(message).build();
    }
}