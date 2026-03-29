package com.easystation.agent.memory.resource;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryImportance;
import com.easystation.agent.memory.domain.Session;
import com.easystation.agent.memory.dto.MemoryDTO;
import com.easystation.agent.memory.dto.SessionDTO;
import com.easystation.agent.memory.spi.MemoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 记忆 REST API
 */
@Path("/api/agent/memories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemoryResource {

    @Inject
    MemoryService memoryService;

    /**
     * 创建会话
     */
    @POST
    @Path("/sessions")
    public Response createSession(@QueryParam("agentId") String agentId,
                                   @QueryParam("userId") String userId) {
        if (agentId == null || agentId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "agentId is required"))
                    .build();
        }
        
        Session session = memoryService.createSession(agentId, userId);
        return Response.ok(SessionDTO.fromSession(session)).build();
    }

    /**
     * 获取会话
     */
    @GET
    @Path("/sessions/{sessionId}")
    public Response getSession(@PathParam("sessionId") String sessionId) {
        Session session = memoryService.getSession(sessionId);
        if (session == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Session not found"))
                    .build();
        }
        return Response.ok(SessionDTO.fromSession(session)).build();
    }

    /**
     * 添加短期记忆
     */
    @POST
    @Path("/short-term")
    public Response addShortTermMemory(@QueryParam("sessionId") String sessionId,
                                        Map<String, String> body) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "sessionId is required"))
                    .build();
        }
        
        String content = body.get("content");
        if (content == null || content.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "content is required"))
                    .build();
        }
        
        memoryService.addShortTermMemory(sessionId, content);
        return Response.ok(Map.of("status", "success")).build();
    }

    /**
     * 获取短期记忆
     */
    @GET
    @Path("/short-term")
    public Response getShortTermMemories(@QueryParam("sessionId") String sessionId,
                                          @DefaultValue("20") @QueryParam("limit") int limit) {
        if (sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        List<Memory> memories = memoryService.getShortTermMemories(sessionId, limit);
        return Response.ok(memories.stream().map(MemoryDTO::fromMemory).collect(Collectors.toList())).build();
    }

    /**
     * 添加长期记忆
     */
    @POST
    @Path("/long-term")
    public Response addLongTermMemory(@QueryParam("sessionId") String sessionId,
                                       Map<String, Object> body) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "sessionId is required"))
                    .build();
        }
        
        String content = (String) body.get("content");
        if (content == null || content.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "content is required"))
                    .build();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        String importanceStr = (String) body.get("importance");
        MemoryImportance importance = importanceStr != null 
                ? MemoryImportance.valueOf(importanceStr.toUpperCase()) 
                : MemoryImportance.MEDIUM;
        
        memoryService.addLongTermMemory(sessionId, content, metadata, importance);
        return Response.ok(Map.of("status", "success")).build();
    }

    /**
     * 搜索长期记忆
     */
    @GET
    @Path("/long-term/search")
    public Response searchLongTermMemories(@QueryParam("sessionId") String sessionId,
                                            @QueryParam("query") String query,
                                            @DefaultValue("10") @QueryParam("limit") int limit) {
        if (sessionId == null || query == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        List<Memory> memories = memoryService.searchLongTermMemories(sessionId, query, limit);
        return Response.ok(memories.stream().map(MemoryDTO::fromMemory).collect(Collectors.toList())).build();
    }

    /**
     * 获取长期记忆
     */
    @GET
    @Path("/long-term")
    public Response getLongTermMemories(@QueryParam("sessionId") String sessionId,
                                         @DefaultValue("50") @QueryParam("limit") int limit) {
        if (sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        List<Memory> memories = memoryService.getLongTermMemories(sessionId, limit);
        return Response.ok(memories.stream().map(MemoryDTO::fromMemory).collect(Collectors.toList())).build();
    }

    /**
     * 删除记忆
     */
    @DELETE
    @Path("/{id}")
    public Response deleteMemory(@PathParam("id") String id) {
        try {
            memoryService.deleteLongTermMemory(id);
            return Response.ok(Map.of("status", "deleted")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Memory not found"))
                    .build();
        }
    }

    /**
     * 获取上下文变量
     */
    @GET
    @Path("/context/{sessionId}")
    public Response getContextVariables(@PathParam("sessionId") String sessionId) {
        Map<String, String> context = memoryService.getAllContextVariables(sessionId);
        return Response.ok(context).build();
    }

    /**
     * 设置上下文变量
     */
    @PUT
    @Path("/context/{sessionId}")
    public Response setContextVariable(@PathParam("sessionId") String sessionId,
                                        Map<String, String> body) {
        for (Map.Entry<String, String> entry : body.entrySet()) {
            memoryService.setContextVariable(sessionId, entry.getKey(), entry.getValue());
        }
        return Response.ok(Map.of("status", "updated")).build();
    }

    /**
     * 压缩记忆
     */
    @POST
    @Path("/compress")
    public Response compressMemories(@QueryParam("sessionId") String sessionId) {
        if (sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        memoryService.autoCompressMemories(sessionId);
        return Response.ok(Map.of("status", "compressed")).build();
    }

    /**
     * 清理会话
     */
    @DELETE
    @Path("/sessions/{sessionId}")
    public Response cleanupSession(@PathParam("sessionId") String sessionId) {
        memoryService.cleanupSession(sessionId);
        return Response.ok(Map.of("status", "cleaned")).build();
    }
}
