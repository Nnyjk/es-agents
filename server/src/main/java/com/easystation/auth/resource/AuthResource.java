package com.easystation.auth.resource;

import com.easystation.system.record.UserRecord;
import com.easystation.auth.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Map;

@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(UserRecord.Login request) {
        return Response.ok(authService.login(request)).build();
    }

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
}
