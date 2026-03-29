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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Path("/auth")
@Tag(name = "认证管理", description = "用户认证 API")
public class AuthResource {

    @Inject
    AuthService authService;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String publicKeyLocation;

    // ========== 登录相关 ==========

    @POST
    @Path("/login")
    @Operation(summary = "用户登录", description = "用户登录认证，返回 JWT Token")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "登录成功，返回 Token"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PermitAll
    public Response login(@Valid AuthRecord.LoginRequest request,
                          @Context HttpHeaders headers) {
        String ipAddress = getClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");
        return Response.ok(authService.login(request, ipAddress, userAgent)).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "注册成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "409", description = "用户名已存在")
    })
    @PermitAll
    public Response register(@Valid AuthRecord.Register request,
                             @Context HttpHeaders headers) {
        return Response.ok(authService.register(request)).build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "用户登出", description = "用户登出，注销当前会话")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "登出成功"),
        @APIResponse(responseCode = "401", description = "未授权")
    })
    public Response logout(@Context SecurityContext securityContext,
                           @Parameter(description = "是否登出所有设备") @QueryParam("allDevices") Boolean allDevices,
                           @Parameter(description = "刷新 Token") @QueryParam("refreshToken") String refreshToken) {
        String username = securityContext.getUserPrincipal().getName();
        authService.logout(username, refreshToken, Boolean.TRUE.equals(allDevices));
        return Response.ok(Map.of("message", "登出成功")).build();
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "刷新 Token", description = "使用刷新 Token 获取新的访问 Token")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "刷新成功，返回新 Token"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "刷新 Token 无效或已过期")
    })
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
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "密码修改成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权或原密码错误")
    })
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
    @Operation(summary = "请求密码重置", description = "请求发送密码重置链接")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "重置链接已发送"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "用户不存在")
    })
    @PermitAll
    public Response requestPasswordReset(@Valid AuthRecord.ResetPasswordRequest request) {
        authService.requestPasswordReset(request);
        return Response.ok(Map.of("message", "重置链接已发送")).build();
    }

    @POST
    @Path("/password/strength")
    @Operation(summary = "检查密码强度", description = "检查密码强度是否符合要求")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回密码强度检查结果"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    @PermitAll
    public Response checkPasswordStrength(@Valid AuthRecord.PasswordStrengthRequest request) {
        return Response.ok(authService.checkPasswordStrength(request.getPassword())).build();
    }

    // ========== 路由 ==========

    @GET
    @Path("/public-key")
    @Operation(summary = "获取公钥", description = "获取 JWT 公钥，用于 Token 验证")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回公钥"),
        @APIResponse(responseCode = "500", description = "无法读取公钥文件")
    })
    @PermitAll
    public Response getPublicKey() {
        try {
            // 读取公钥文件
            String publicKey = Files.readString(java.nio.file.Path.of(publicKeyLocation));
            return Response.ok(Map.of("publicKey", publicKey.trim())).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "无法读取公钥文件"))
                    .build();
        }
    }

    @GET
    @Path("/routes")
    @Operation(summary = "获取用户路由", description = "获取当前用户可访问的前端路由")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回用户路由列表"),
        @APIResponse(responseCode = "401", description = "未授权")
    })
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