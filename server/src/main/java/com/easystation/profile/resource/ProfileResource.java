package com.easystation.profile.resource;

import com.easystation.profile.dto.ProfileRecord;
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

@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource {

    @Inject
    ProfileService profileService;

    @Inject
    AuditLogService auditLogService;

    @GET
    @Path("/profile")
    public Response getProfile(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(profileService.getProfile(userId)).build();
    }

    @PUT
    @Path("/profile")
    public Response updateProfile(
            @Context SecurityContext securityContext,
            @Valid ProfileRecord.Update dto) {
        UUID userId = getCurrentUserId(securityContext);
        long start = System.currentTimeMillis();
        
        ProfileRecord profile = profileService.updateProfile(userId, dto);
        
        auditLogService.logSuccess(
            userId, "PROFILE_UPDATE", "USER", userId.toString(),
            "Updated personal profile", 
            null, null, 
            System.currentTimeMillis() - start
        );
        
        return Response.ok(profile).build();
    }

    @PUT
    @Path("/password")
    public Response changePassword(
            @Context SecurityContext securityContext,
            @Valid ProfileRecord.PasswordChange dto) {
        UUID userId = getCurrentUserId(securityContext);
        long start = System.currentTimeMillis();
        
        profileService.changePassword(userId, dto);
        
        auditLogService.logSuccess(
            userId, "PASSWORD_CHANGE", "USER", userId.toString(),
            "Changed password", 
            null, null, 
            System.currentTimeMillis() - start
        );
        
        return Response.ok().entity(java.util.Map.of("message", "Password changed successfully")).build();
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