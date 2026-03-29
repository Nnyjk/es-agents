package com.easystation.profile.resource;

import com.easystation.auth.annotation.RequiresPermission;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "个人中心", description = "用户个人资料与账户设置 API")
public class ProfileResource {

    @Inject
    ProfileService profileService;

    @Inject
    AuditLogService auditLogService;

    @GET
    @Path("/profile")
    @Operation(summary = "获取个人资料", description = "获取当前登录用户的个人信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回个人资料"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:read")
    public Response getProfile(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);
        return Response.ok(profileService.getProfile(userId)).build();
    }

    @PUT
    @Path("/profile")
    @Operation(summary = "更新个人资料", description = "更新当前用户的个人信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "资料更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
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
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "密码修改成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效（原密码错误）"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("profile:write")
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
