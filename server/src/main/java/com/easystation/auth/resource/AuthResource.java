package com.easystation.auth.resource;

import com.easystation.auth.dto.AuthRecord;
import com.easystation.auth.service.PasswordService;
import com.easystation.auth.service.SessionService;
import com.easystation.auth.service.UserRegistrationService;
import com.easystation.system.domain.User;
import com.easystation.system.record.UserRecord;
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
import java.util.UUID;

@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    UserRegistrationService userRegistrationService;

    @Inject
    SessionService sessionService;

    @Inject
    PasswordService passwordService;

    // ========== 登录相关 ==========

    /**
     * 用户注册
     */
    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid AuthRecord.RegisterRequest request,
                             @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        return Response.ok(userRegistrationService.register(request, ipAddress, userAgent)).build();
    }

    /**
     * 用户登录
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid AuthRecord.LoginRequest request,
                          @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        return Response.ok(authService.login(request, ipAddress, userAgent)).build();
    }

    /**
     * 用户登出
     */
    @POST
    @Path("/logout")
    public Response logout(@Valid AuthRecord.LogoutRequest request,
                           @Context SecurityContext securityContext,
                           @Context HttpHeaders headers) {
        String username = securityContext.getUserPrincipal().getName();
        User user = User.find("username", username).firstResult();
        String token = extractToken(headers);
        String ipAddress = getClientIp(headers);

        userRegistrationService.logout(token, user.id,
            request != null && Boolean.TRUE.equals(request.getAllDevices()), ipAddress);
        return Response.ok(Map.of("message", "登出成功")).build();
    }

    /**
     * 刷新访问令牌
     */
    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(@Valid AuthRecord.RefreshTokenRequest request) {
        return Response.ok(userRegistrationService.refreshToken(request.refreshToken())).build();
    }

    // ========== 会话管理 ==========

    /**
     * 获取当前用户的所有会话
     */
    @GET
    @Path("/sessions")
    public Response getSessions(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        User user = User.find("username", username).firstResult();
        return Response.ok(sessionService.getActiveSessions(user.id)).build();
    }

    /**
     * 使指定会话失效
     */
    @DELETE
    @Path("/sessions/{sessionId}")
    public Response invalidateSession(@PathParam("sessionId") UUID sessionId,
                                      @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        User user = User.find("username", username).firstResult();
        sessionService.invalidateSession(sessionId, user.id);
        return Response.ok(Map.of("message", "会话已失效")).build();
    }

    /**
     * 使所有会话失效
     */
    @DELETE
    @Path("/sessions")
    public Response invalidateAllSessions(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        User user = User.find("username", username).firstResult();
        sessionService.invalidateAllSessions(user.id);
        return Response.ok(Map.of("message", "所有会话已失效")).build();
    }

    // ========== 密码管理 ==========

    /**
     * 修改密码
     */
    @POST
    @Path("/password/change")
    public Response changePassword(@Valid AuthRecord.ChangePasswordRequest request,
                                   @Context SecurityContext securityContext,
                                   @Context HttpHeaders headers) {
        String username = securityContext.getUserPrincipal().getName();
        User user = User.find("username", username).firstResult();
        String ipAddress = getClientIp(headers);

        passwordService.changePassword(user.id, request, ipAddress);
        return Response.ok(Map.of("message", "密码修改成功")).build();
    }

    /**
     * 忘记密码
     */
    @POST
    @Path("/password/forgot")
    @PermitAll
    public Response forgotPassword(@Valid AuthRecord.ForgotPasswordRequest request,
                                   @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        passwordService.initiatePasswordReset(request.email(), ipAddress);
        return Response.ok(Map.of("message", "重置链接已发送到邮箱")).build();
    }

    /**
     * 重置密码
     */
    @POST
    @Path("/password/reset")
    @PermitAll
    public Response resetPassword(@Valid AuthRecord.ResetPasswordRequest request,
                                  @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        passwordService.resetPassword(request, ipAddress);
        return Response.ok(Map.of("message", "密码重置成功")).build();
    }

    /**
     * 检查密码强度
     */
    @POST
    @Path("/password/strength")
    @PermitAll
    public Response checkPasswordStrength(Map<String, String> request) {
        String password = request.get("password");
        return Response.ok(passwordService.checkPasswordStrength(password)).build();
    }

    // ========== 路由与公钥 ==========

    /**
     * 获取用户可访问的路由
     */
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

    /**
     * 获取 JWT 公钥
     */
    @GET
    @Path("/public-key")
    @PermitAll
    public Response getPublicKey() {
        return Response.ok(Map.of("publicKey", "")).build();
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

    private String extractToken(HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}