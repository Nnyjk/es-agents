package com.easystation.profile.resource;

import com.easystation.profile.dto.PreferenceRecord;
import com.easystation.profile.service.AuditLogService;
import com.easystation.profile.service.ProfileService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

@Path("/me/preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreferenceResource {

    @Inject
    ProfileService profileService;

    @Inject
    AuditLogService auditLogService;

    @GET
    public Response getPreferences(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(profileService.getPreference(userId)).build();
    }

    @PUT
    public Response updatePreferences(
            @Context SecurityContext securityContext,
            @Valid PreferenceRecord.Update dto) {
        UUID userId = getCurrentUserId(securityContext);
        long start = System.currentTimeMillis();
        
        PreferenceRecord prefs = profileService.updatePreference(userId, dto);
        
        auditLogService.logSuccess(
            userId, "PREFERENCE_UPDATE", "USER_PREFERENCE", userId.toString(),
            "Updated user preferences", 
            null, null, 
            System.currentTimeMillis() - start
        );
        
        return Response.ok(prefs).build();
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
}