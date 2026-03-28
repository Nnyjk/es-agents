package com.easystation.auth.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.auth.dto.ApiKeyRecord;
import com.easystation.auth.service.ApiKeyService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api/v1/api-keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiKeyResource {

    @Inject
    ApiKeyService apiKeyService;

    @GET
    @RequiresPermission("api-key:view")
    public Response list(
            @QueryParam("keyword") String keyword,
            @QueryParam("createdBy") UUID createdBy,
            @QueryParam("enabled") Boolean enabled,
            @QueryParam("expired") Boolean expired,
            @QueryParam("revoked") Boolean revoked,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ApiKeyRecord.Query query = new ApiKeyRecord.Query(
                keyword, createdBy, enabled, expired, revoked, limit, offset
        );
        return Response.ok(apiKeyService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("api-key:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(apiKeyService.get(id)).build();
    }

    @POST
    @RequiresPermission("api-key:create")
    public Response create(@Valid ApiKeyRecord.Create dto) {
        ApiKeyRecord.Detail key = apiKeyService.create(dto);
        // Only return the full key (with secret) on creation
        return Response.status(Response.Status.CREATED).entity(key).build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("api-key:edit")
    public Response update(@PathParam("id") UUID id, @Valid ApiKeyRecord.Update dto) {
        return Response.ok(apiKeyService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("api-key:delete")
    public Response delete(@PathParam("id") UUID id) {
        apiKeyService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/revoke")
    @RequiresPermission("api-key:revoke")
    public Response revoke(@PathParam("id") UUID id, @Valid ApiKeyRecord.RevokeRequest dto) {
        return Response.ok(apiKeyService.revoke(id, dto)).build();
    }

    @POST
    @Path("/{id}/refresh")
    @RequiresPermission("api-key:refresh")
    public Response refresh(@PathParam("id") UUID id, @Valid ApiKeyRecord.RefreshRequest dto) {
        // Returns the new key (with secret) only on refresh
        return Response.ok(apiKeyService.refresh(id, dto)).build();
    }

    @POST
    @Path("/validate")
    public Response validate(
            @QueryParam("key") String key,
            @QueryParam("clientIp") String clientIp,
            @QueryParam("permission") String permission) {
        if (key == null || key.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiKeyRecord.ValidationResult(false, null, null, null, "API Key is required"))
                    .build();
        }
        return Response.ok(apiKeyService.validate(key, clientIp, permission)).build();
    }

    @GET
    @Path("/{id}/logs")
    @RequiresPermission("api-key:view")
    public Response getUsageLogs(
            @PathParam("id") UUID keyId,
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
        ApiKeyRecord.UsageLogQuery query = new ApiKeyRecord.UsageLogQuery(
                keyId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiKeyService.getUsageLogs(query)).build();
    }

    @GET
    @Path("/logs")
    @RequiresPermission("api-key:view")
    public Response getAllUsageLogs(
            @QueryParam("keyId") UUID keyId,
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
        ApiKeyRecord.UsageLogQuery query = new ApiKeyRecord.UsageLogQuery(
                keyId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiKeyService.getUsageLogs(query)).build();
    }
}