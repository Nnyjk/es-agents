package com.easystation.profile.resource;

import com.easystation.auth.annotation.RequiresPermission;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/v1/me/preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "用户偏好设置", description = "用户个性化配置与偏好设置 API")
public class PreferenceResource {

    @Inject
    ProfileService profileService;

    @Inject
    AuditLogService auditLogService;

    @GET
    @Operation(summary = "获取偏好设置", description = "获取当前用户的个性化偏好配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回偏好设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response getPreferences(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(profileService.getPreference(userId)).build();
    }

    @PUT
    @Operation(summary = "更新偏好设置", description = "更新当前用户的个性化偏好配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "偏好设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
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
