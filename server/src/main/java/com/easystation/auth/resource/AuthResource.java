package com.easystation.auth.resource;

import com.easystation.auth.dto.AuthRecord;
import com.easystation.auth.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    // ========== 登录相关 ==========

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid AuthRecord.LoginRequest request,
                          @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        return Response.ok(authService.login(request, ipAddress, userAgent)).build();
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid AuthRecord.Register request,
                             @Context HttpHeaders headers) {
        return Response.ok(authService.register(request)).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context SecurityContext securityContext,
                           @QueryParam("allDevices") Boolean allDevices,
                           @QueryParam("refreshToken") String refreshToken) {
        String username = securityContext.getUserPrincipal().getName();
        authService.logout(username, refreshToken, Boolean.TRUE.equals(allDevices));
        return Response.ok(Map.of("message", "登出成功")).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(@Valid AuthRecord.RefreshTokenRequest request,
                                @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        return Response.ok(authService.refreshToken(request.refreshToken(), ipAddress, userAgent)).build();
    }

    // ========== 密码管理 ==========

    @POST
    @Path("/password/change")
    public Response changePassword(@Valid AuthRecord.ChangePassword request,
                                   @Context SecurityContext securityContext,
                                   @Context HttpHeaders headers) {
        String username = securityContext.getUserPrincipal().getName();
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        authService.changePassword(username, request, ipAddress, userAgent);
        return Response.ok(Map.of("message", "密码修改成功")).build();
    }

    @POST
    @Path("/password/reset")
    @PermitAll
    public Response requestPasswordReset(@Valid AuthRecord.ResetPasswordRequest request) {
        authService.requestPasswordReset(request);
        return Response.ok(Map.of("message", "重置链接已发送")).build();
    }

    @POST
    @Path("/password/strength")
    @PermitAll
    public Response checkPasswordStrength(@Valid AuthRecord.PasswordStrengthRequest request) {
        return Response.ok(authService.checkPasswordStrength(request.getPassword())).build();
    }

    // ========== 路由 ==========

    @GET
    @Path("/routes")
    public Response getRoutes(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null ? 
            securityContext.getUserPrincipal().getName() : null;
        if (username == null) {
            return Response.ok(List.of()).build();
        }
        return Response.ok(authService.getRoutes(username)).build();
    }

    // ========== 辅助方法 ==========

    private String getClientIp(HttpHeaders headers) {
        String ip = headers.getHeaderString("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = headers.getHeaderString("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = "unknown";
        }
        return ip.split(",")[0].trim();
    }
}