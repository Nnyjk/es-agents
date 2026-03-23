package com.easystation.profile.resource;

import com.easystation.profile.dto.SessionRecord;
import com.easystation.profile.service.AuditLogService;
import com.easystation.profile.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/me/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject
    SessionService sessionService;

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response listSessions(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        String currentTokenId = extractTokenId(securityContext);
        List<SessionRecord> sessions = sessionService.listSessions(userId, currentTokenId);
        return Response.ok(sessions).build();
    }

    @GET
    @Path("/summary")
    public Response getSessionSummary(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(sessionService.getSessionSummary(userId)).build();
    }

    @DELETE
    @Path("/{sessionId}")
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