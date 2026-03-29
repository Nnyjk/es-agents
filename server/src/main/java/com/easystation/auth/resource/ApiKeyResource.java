package com.easystation.auth.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.auth.dto.ApiKeyRecord;
import com.easystation.auth.service.ApiKeyService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api/v1/api-keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Key 管理", description = "API Key 管理 API")
public class ApiKeyResource {

    @Inject
    ApiKeyService apiKeyService;

    @GET
    @Operation(summary = "获取 API Key 列表", description = "分页查询 API Key 列表，支持按关键字、创建者、状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回 API Key 列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:view")
    public Response list(
            @Parameter(description = "关键字（模糊匹配）") @QueryParam("keyword") String keyword,
            @Parameter(description = "创建者 ID") @QueryParam("createdBy") UUID createdBy,
            @Parameter(description = "是否启用") @QueryParam("enabled") Boolean enabled,
            @Parameter(description = "是否过期") @QueryParam("expired") Boolean expired,
            @Parameter(description = "是否已吊销") @QueryParam("revoked") Boolean revoked,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        ApiKeyRecord.Query query = new ApiKeyRecord.Query(
                keyword, createdBy, enabled, expired, revoked, limit, offset
        );
        return Response.ok(apiKeyService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取 API Key 详情", description = "根据 ID 查询 API Key 详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回 API Key 详情"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:view")
    public Response get(@Parameter(description = "API Key ID") @PathParam("id") UUID id) {
        return Response.ok(apiKeyService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建 API Key", description = "创建新的 API Key")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建 API Key"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:create")
    public Response create(@Valid ApiKeyRecord.Create dto) {
        ApiKeyRecord.Detail key = apiKeyService.create(dto);
        // Only return the full key (with secret) on creation
        return Response.status(Response.Status.CREATED).entity(key).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新 API Key", description = "更新指定 API Key 的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新 API Key"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:edit")
    public Response update(@Parameter(description = "API Key ID") @PathParam("id") UUID id, @Valid ApiKeyRecord.Update dto) {
        return Response.ok(apiKeyService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除 API Key", description = "删除指定的 API Key")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除 API Key"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:delete")
    public Response delete(@Parameter(description = "API Key ID") @PathParam("id") UUID id) {
        apiKeyService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/revoke")
    @Operation(summary = "吊销 API Key", description = "吊销指定的 API Key")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功吊销 API Key"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:revoke")
    public Response revoke(@Parameter(description = "API Key ID") @PathParam("id") UUID id, @Valid ApiKeyRecord.RevokeRequest dto) {
        return Response.ok(apiKeyService.revoke(id, dto)).build();
    }

    @POST
    @Path("/{id}/refresh")
    @Operation(summary = "刷新 API Key", description = "刷新指定的 API Key，生成新的密钥")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功刷新 API Key"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:refresh")
    public Response refresh(@Parameter(description = "API Key ID") @PathParam("id") UUID id, @Valid ApiKeyRecord.RefreshRequest dto) {
        // Returns the new key (with secret) only on refresh
        return Response.ok(apiKeyService.refresh(id, dto)).build();
    }

    @POST
    @Path("/validate")
    @Operation(summary = "验证 API Key", description = "验证 API Key 是否有效")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回验证结果"),
        @APIResponse(responseCode = "400", description = "API Key 不能为空")
    })
    public Response validate(
            @Parameter(description = "API Key", required = true) @QueryParam("key") String key,
            @Parameter(description = "客户端 IP") @QueryParam("clientIp") String clientIp,
            @Parameter(description = "需要检查的权限") @QueryParam("permission") String permission) {
        if (key == null || key.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiKeyRecord.ValidationResult(false, null, null, null, "API Key is required"))
                    .build();
        }
        return Response.ok(apiKeyService.validate(key, clientIp, permission)).build();
    }

    @GET
    @Path("/{id}/logs")
    @Operation(summary = "获取 API Key 使用日志", description = "查询指定 API Key 的使用日志")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回使用日志列表"),
        @APIResponse(responseCode = "404", description = "API Key 不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:view")
    public Response getUsageLogs(
            @Parameter(description = "API Key ID") @PathParam("id") UUID keyId,
            @Parameter(description = "客户端 IP") @QueryParam("clientIp") String clientIp,
            @Parameter(description = "请求方法") @QueryParam("method") String method,
            @Parameter(description = "请求路径") @QueryParam("path") String path,
            @Parameter(description = "响应状态码") @QueryParam("status") Integer status,
            @Parameter(description = "开始时间（ISO 格式）") @QueryParam("startTime") String startTimeStr,
            @Parameter(description = "结束时间（ISO 格式）") @QueryParam("endTime") String endTimeStr,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ApiKeyRecord.UsageLogQuery query = new ApiKeyRecord.UsageLogQuery(
                keyId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiKeyService.getUsageLogs(query)).build();
    }

    @GET
    @Path("/logs")
    @Operation(summary = "获取所有 API Key 使用日志", description = "查询所有 API Key 的使用日志")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回使用日志列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("api-key:view")
    public Response getAllUsageLogs(
            @Parameter(description = "API Key ID") @QueryParam("keyId") UUID keyId,
            @Parameter(description = "客户端 IP") @QueryParam("clientIp") String clientIp,
            @Parameter(description = "请求方法") @QueryParam("method") String method,
            @Parameter(description = "请求路径") @QueryParam("path") String path,
            @Parameter(description = "响应状态码") @QueryParam("status") Integer status,
            @Parameter(description = "开始时间（ISO 格式）") @QueryParam("startTime") String startTimeStr,
            @Parameter(description = "结束时间（ISO 格式）") @QueryParam("endTime") String endTimeStr,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ApiKeyRecord.UsageLogQuery query = new ApiKeyRecord.UsageLogQuery(
                keyId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiKeyService.getUsageLogs(query)).build();
    }
}