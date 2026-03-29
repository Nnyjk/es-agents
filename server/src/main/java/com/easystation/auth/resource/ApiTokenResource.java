package com.easystation.auth.resource;

import com.easystation.auth.dto.ApiTokenRecord;
import com.easystation.auth.service.ApiTokenService;
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

@Path("/api-tokens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Token 管理", description = "API Token 管理 API")
public class ApiTokenResource {

    @Inject
    ApiTokenService apiTokenService;

    @GET
    @Operation(summary = "获取 API Token 列表", description = "分页查询 API Token 列表，支持按关键字、用户、范围、状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回 API Token 列表")
    })
    public Response list(
            @Parameter(description = "关键字（模糊匹配）") @QueryParam("keyword") String keyword,
            @Parameter(description = "用户 ID") @QueryParam("userId") UUID userId,
            @Parameter(description = "Token 作用范围") @QueryParam("scope") String scope,
            @Parameter(description = "是否已吊销") @QueryParam("revoked") Boolean revoked,
            @Parameter(description = "是否已过期") @QueryParam("expired") Boolean expired,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        ApiTokenRecord.Query query = new ApiTokenRecord.Query(
                keyword, userId,
                scope != null ? com.easystation.auth.domain.enums.TokenScope.valueOf(scope) : null,
                revoked, expired, limit, offset
        );
        return Response.ok(apiTokenService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取 API Token 详情", description = "根据 ID 查询 API Token 详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回 API Token 详情"),
        @APIResponse(responseCode = "404", description = "API Token 不存在")
    })
    public Response get(@Parameter(description = "API Token ID") @PathParam("id") UUID id) {
        return Response.ok(apiTokenService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建 API Token", description = "创建新的 API Token")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建 API Token"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response create(@Valid ApiTokenRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(apiTokenService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新 API Token", description = "更新指定 API Token 的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新 API Token"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "API Token 不存在")
    })
    public Response update(@Parameter(description = "API Token ID") @PathParam("id") UUID id, @Valid ApiTokenRecord.Update dto) {
        return Response.ok(apiTokenService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除 API Token", description = "删除指定的 API Token")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除 API Token"),
        @APIResponse(responseCode = "404", description = "API Token 不存在")
    })
    public Response delete(@Parameter(description = "API Token ID") @PathParam("id") UUID id) {
        apiTokenService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/revoke")
    @Operation(summary = "吊销 API Token", description = "吊销指定的 API Token")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功吊销 API Token"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "API Token 不存在")
    })
    public Response revoke(@Parameter(description = "API Token ID") @PathParam("id") UUID id, @Valid ApiTokenRecord.RevokeRequest dto) {
        return Response.ok(apiTokenService.revoke(id, dto)).build();
    }

    @POST
    @Path("/validate")
    @Operation(summary = "验证 API Token", description = "验证 API Token 是否有效")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回验证结果"),
        @APIResponse(responseCode = "400", description = "Token 不能为空")
    })
    public Response validate(@Parameter(description = "Token", required = true) @QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiTokenRecord.TokenValidation(false, null, null, null, "Token is required"))
                    .build();
        }
        return Response.ok(apiTokenService.validate(token)).build();
    }

    @GET
    @Path("/{id}/logs")
    @Operation(summary = "获取 API Token 访问日志", description = "查询指定 API Token 的访问日志")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回访问日志列表"),
        @APIResponse(responseCode = "404", description = "API Token 不存在")
    })
    public Response getAccessLogs(
            @Parameter(description = "API Token ID") @PathParam("id") UUID tokenId,
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
        ApiTokenRecord.AccessLogQuery query = new ApiTokenRecord.AccessLogQuery(
                tokenId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiTokenService.getAccessLogs(query)).build();
    }

    @GET
    @Path("/logs")
    @Operation(summary = "获取所有 API Token 访问日志", description = "查询所有 API Token 的访问日志")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回访问日志列表")
    })
    public Response getAllAccessLogs(
            @Parameter(description = "API Token ID") @QueryParam("tokenId") UUID tokenId,
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
        ApiTokenRecord.AccessLogQuery query = new ApiTokenRecord.AccessLogQuery(
                tokenId, clientIp, method, path, status, startTime, endTime, limit, offset
        );
        return Response.ok(apiTokenService.getAccessLogs(query)).build();
    }
}