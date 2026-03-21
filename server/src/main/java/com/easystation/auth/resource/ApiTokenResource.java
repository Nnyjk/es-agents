package com.easystation.auth.resource;

import com.easystation.auth.dto.ApiTokenRecord;
import com.easystation.auth.service.ApiTokenService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api-tokens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiTokenResource {

    @Inject
    ApiTokenService apiTokenService;

    @GET
    public Response list(
            @QueryParam("keyword") String keyword,
            @QueryParam("userId") UUID userId,
            @QueryParam("scope") String scope,
            @QueryParam("revoked") Boolean revoked,
            @QueryParam("expired") Boolean expired,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ApiTokenRecord.Query query = new ApiTokenRecord.Query(
                keyword, userId, 
                scope != null ? com.easystation.auth.domain.enums.TokenScope.valueOf(scope) : null,
                revoked, expired, limit, offset
        );
        return Response.ok(apiTokenService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(apiTokenService.get(id)).build();
    }

    @POST
    public Response create(@Valid ApiTokenRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(apiTokenService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ApiTokenRecord.Update dto) {
        return Response.ok(apiTokenService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        apiTokenService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/revoke")
    public Response revoke(@PathParam("id") UUID id, @Valid ApiTokenRecord.RevokeRequest dto) {
        return Response.ok(apiTokenService.revoke(id, dto)).build();
    }

    @POST
    @Path("/validate")
    public Response validate(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiTokenRecord.TokenValidation(false, null, null, null, "Token is required"))
                    .build();
        }
        return Response.ok(apiTokenService.validate(token)).build();
    }

    @GET
    @Path("/{id}/logs")
    public Response getAccessLogs(
            @PathParam("id") UUID tokenId,
            @QueryParam("clientIp") String clientIp,
            @QueryParam("method") String method,
            @QueryParam("path") String path,
            @QueryParam("status") Integer status,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ApiTokenRecord.AccessLogQuery query = new ApiTokenRecord.AccessLogQuery(
                tokenId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiTokenService.getAccessLogs(query)).build();
    }

    @GET
    @Path("/logs")
    public Response getAllAccessLogs(
            @QueryParam("tokenId") UUID tokenId,
            @QueryParam("clientIp") String clientIp,
            @QueryParam("method") String method,
            @QueryParam("path") String path,
            @QueryParam("status") Integer status,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ApiTokenRecord.AccessLogQuery query = new ApiTokenRecord.AccessLogQuery(
                tokenId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiTokenService.getAccessLogs(query)).build();
    }
}