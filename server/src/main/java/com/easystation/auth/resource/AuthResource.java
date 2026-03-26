package com.easystation.auth.resource;

import com.easystation.auth.dto.AuthRecord;
import com.easystation.auth.service.AuthService;
import com.easystation.system.record.UserRecord;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Map;

@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    /**
     * 用户注册
     */
    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid AuthRecord.Register request) {
        UserRecord user = authService.register(request);
        return Response.status(Response.Status.CREATED)
            .entity(Map.of(
                "message", "注册成功",
                "user", user
            ))
            .build();
    }

    /**
     * 用户登录
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid AuthRecord.LoginRequest request,
                          @Context HttpHeaders headers,
                          @Context SecurityContext securityContext) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");

        AuthRecord.ExtendedLoginResponse response = authService.login(request, ipAddress, userAgent);
        return Response.ok(response).build();
    }

    /**
     * 用户登出
     */
    @POST
    @Path("/logout")
    public Response logout(@Context SecurityContext securityContext,
                          AuthRecord.Logout request) {
        String username = securityContext.getUserPrincipal().getName();
        authService.logout(username, null, request != null && Boolean.TRUE.equals(request.allDevices()));
        return Response.ok(Map.of("message", "登出成功")).build();
    }

    /**
     * 刷新访问令牌
     */
    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(@Valid AuthRecord.RefreshTokenRequest request,
                                 @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");

        AuthRecord.TokenResponse response = authService.refreshToken(
            request.refreshToken(), ipAddress, userAgent
        );
        return Response.ok(response).build();
    }

    /**
     * 修改密码
     */
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

    /**
     * 请求密码重置
     */
    @POST
    @Path("/password/reset/request")
    @PermitAll
    public Response requestPasswordReset(@Valid AuthRecord.ResetPasswordRequest request) {
        authService.requestPasswordReset(request);
        // 为了安全，总是返回成功消息
        return Response.ok(Map.of(
            "message", "如果该邮箱/手机号已注册，您将收到密码重置链接"
        )).build();
    }

    /**
     * 验证密码重置
     */
    @POST
    @Path("/password/reset/verify")
    @PermitAll
    public Response verifyPasswordReset(@Valid AuthRecord.ResetPasswordVerify request) {
        authService.verifyPasswordReset(request);
        return Response.ok(Map.of("message", "密码重置成功")).build();
    }

    /**
     * 检查密码强度
     */
    @POST
    @Path("/password/strength")
    @PermitAll
    public Response checkPasswordStrength(@Valid AuthRecord.CheckPasswordStrength request) {
        AuthRecord.PasswordStrengthResponse response = authService.checkPasswordStrength(request.password());
        return Response.ok(response).build();
    }

    /**
     * 获取用户路由
     */
    @GET
    @Path("/routes")
    public Response getRoutes(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) {
            return Response.ok(java.util.List.of()).build();
        }
        return Response.ok(authService.getRoutes(username)).build();
    }

    /**
     * 获取公钥
     * 暂时剔除登录加密功能，返回硬编码占位公钥
     */
    @GET
    @Path("/public-key")
    @PermitAll
    public Response getPublicKey() {
        // 暂时剔除公钥加密功能，返回硬编码占位值
        String hardcodedPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDUMMY+PLACEHOLDER+KEY+FOR+\n" +
                "LOGIN+ENCRYPTION+DISABLED+TEMPORARILY+USE+PLAINTEXT+PASSWORD+INSTEAD+\n" +
                "THIS+IS+HARDCODED+PERMANENT+PUBLIC+KEY+FOR+COMPATIBILITY+ONLY\n" +
                "-----END PUBLIC KEY-----";
        return Response.ok(Map.of("publicKey", hardcodedPublicKey)).build();
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpHeaders headers) {
        // 尝试从代理头获取真实 IP
        String xForwardedFor = headers.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = headers.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 如果没有代理头，返回默认值
        return "unknown";
    }
}
