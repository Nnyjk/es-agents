package com.easystation.profile.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.profile.dto.SessionRecord;
import com.easystation.profile.service.AuditLogService;
import com.easystation.profile.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/me/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "会话管理", description = "用户登录会话管理 API")
public class SessionResource {

    @Inject
    SessionService sessionService;

    @Inject
    AuditLogService auditLogService;

    @GET
    @Operation(summary = "列出会话", description = "获取当前用户的所有登录会话")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回会话列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response listSessions(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        String currentTokenId = extractTokenId(securityContext);
        List<SessionRecord> sessions = sessionService.listSessions(userId, currentTokenId);
        return Response.ok(sessions).build();
    }

    @GET
    @Path("/summary")
    @Operation(summary = "获取会话摘要", description = "获取当前用户的会话统计信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回会话摘要"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response getSessionSummary(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(sessionService.getSessionSummary(userId)).build();
    }

    @DELETE
    @Path("/{sessionId}")
    @Operation(summary = "终止会话", description = "终止指定的登录会话")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "会话终止成功"),
        @APIResponse(responseCode = "404", description = "会话不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "无权终止此会话")
    })
    @RequiresPermission("profile:write")
    public Response terminateSession(
            @Context SecurityContext securityContext,
            @PathParam("sessionId") UUID sessionId) {
        UUID userId = getCurrentUserId(securityContext);
        String currentTokenId = extractTokenId(securityContext);
        
        sessionService.terminateSession(userId, sessionId, currentTokenId);
        
        auditLogService.logSuccess(
            userId, "SESSION_TERMINATE", "USER_SESSION", sessionId.toString(),
            "Terminated login session", 
            null, null, null
        );
        
        return Response.ok().entity(java.util.Map.of("message", "Session terminated")).build();
    }

    @DELETE
    @Path("/others")
    @Operation(summary = "终止其他会话", description = "终止当前用户的所有其他登录会话（保留当前会话）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "会话终止成功"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
    public Response terminateAllOtherSessions(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        String currentTokenId = extractTokenId(securityContext);
        
        sessionService.terminateAllOtherSessions(userId, currentTokenId);
        
        auditLogService.logSuccess(
            userId, "SESSION_TERMINATE_ALL", "USER_SESSION", null,
            "Terminated all other login sessions", 
            null, null, null
        );
        
        return Response.ok().entity(java.util.Map.of("message", "All other sessions terminated")).build();
    }

    private UUID getCurrentUserId(SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(securityContext.getUserPrincipal().getName());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid user identity", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractTokenId(SecurityContext securityContext) {
        // Extract from JWT token or session
        // For now, return null as we need token service integration
        return null;
    }
}
