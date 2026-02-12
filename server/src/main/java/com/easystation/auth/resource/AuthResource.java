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

import java.nio.charset.StandardCharsets;
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
     * 用于前端加密敏感信息或验证Token签名
     */
    @GET
    @Path("/public-key")
    @PermitAll
    public Response getPublicKey() {
        try (java.io.InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("publicKey.pem")) {
            if (is == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Public key not found").build();
            }
            String publicKey = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Response.ok(Map.of("publicKey", publicKey)).build();
        } catch (java.io.IOException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
