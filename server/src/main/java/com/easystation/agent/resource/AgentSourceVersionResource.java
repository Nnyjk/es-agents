package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentSourceVersionRecord;
import com.easystation.agent.service.AgentSourceVersionService;
import com.easystation.auth.annotation.RequiresPermission;
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

import java.util.UUID;

@Path("/agents/sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 源码版本管理", description = "Agent 源码版本、缓存、拉取 API")
public class AgentSourceVersionResource {

    @Inject
    AgentSourceVersionService versionService;

    @GET
    @Path("/{sourceId}/versions")
    @Operation(summary = "获取源码版本列表", description = "支持按版本号、验证状态筛选，支持分页")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "sourceId", description = "源码 ID", required = true)
    @Parameter(name = "version", description = "版本号筛选（可选）", required = false)
    @Parameter(name = "verified", description = "验证状态筛选（可选）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（可选）", required = false)
    @Parameter(name = "offset", description = "偏移量（可选）", required = false)
    @RequiresPermission("agent:view")
    public Response listVersions(
            @PathParam("sourceId") UUID sourceId,
            @QueryParam("version") String version,
            @QueryParam("verified") Boolean verified,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        AgentSourceVersionRecord.Query query = new AgentSourceVersionRecord.Query(
                sourceId, version, verified, limit, offset
        );
        return Response.ok(versionService.listVersions(query)).build();
    }

    @GET
    @Path("/versions/{id}")
    @Operation(summary = "获取单个版本详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本详情"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "版本 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getVersion(@PathParam("id") UUID id) {
        return Response.ok(versionService.getVersion(id)).build();
    }

    @POST
    @Path("/{sourceId}/versions")
    @Operation(summary = "创建新的源码版本")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建版本"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "sourceId", description = "源码 ID", required = true)
    @Parameter(name = "dto", description = "版本创建请求", required = true)
    @RequiresPermission("agent:create")
    public Response createVersion(
            @PathParam("sourceId") UUID sourceId,
            @Valid AgentSourceVersionRecord.Create dto) {
        AgentSourceVersionRecord.Create createDto = new AgentSourceVersionRecord.Create(
                sourceId, dto.version(), dto.filePath(), dto.fileSize(),
                dto.checksumMd5(), dto.checksumSha256(), dto.description(),
                dto.downloadUrl(), dto.createdBy()
        );
        return Response.status(Response.Status.CREATED)
                .entity(versionService.createVersion(createDto))
                .build();
    }

    @PUT
    @Path("/versions/{id}")
    @Operation(summary = "更新版本配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新版本"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "版本 ID", required = true)
    @Parameter(name = "dto", description = "版本更新请求", required = true)
    @RequiresPermission("agent:edit")
    public Response updateVersion(@PathParam("id") UUID id, @Valid AgentSourceVersionRecord.Update dto) {
        return Response.ok(versionService.updateVersion(id, dto)).build();
    }

    @DELETE
    @Path("/versions/{id}")
    @Operation(summary = "删除版本")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除版本"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "版本 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response deleteVersion(@PathParam("id") UUID id) {
        versionService.deleteVersion(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/versions/{id}/verify")
    @Operation(summary = "验证版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回验证结果"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "版本 ID", required = true)
    @Parameter(name = "dto", description = "验证请求", required = true)
    @RequiresPermission("agent:execute")
    public Response verifyVersion(@PathParam("id") UUID id, @Valid AgentSourceVersionRecord.VerifyRequest dto) {
        return Response.ok(versionService.verifyVersion(id, dto)).build();
    }

    @GET
    @Path("/{sourceId}/cache")
    @Operation(summary = "获取源码缓存列表", description = "支持按有效性筛选，支持分页")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回缓存列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "sourceId", description = "源码 ID", required = true)
    @Parameter(name = "valid", description = "有效性筛选（可选）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（可选）", required = false)
    @Parameter(name = "offset", description = "偏移量（可选）", required = false)
    @RequiresPermission("agent:view")
    public Response listCache(
            @PathParam("sourceId") UUID sourceId,
            @QueryParam("valid") Boolean valid,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        AgentSourceVersionRecord.CacheQuery query = new AgentSourceVersionRecord.CacheQuery(
                sourceId, valid, limit, offset
        );
        return Response.ok(versionService.listCache(query)).build();
    }

    @POST
    @Path("/{sourceId}/pull")
    @Operation(summary = "拉取源码")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功拉取源码"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "sourceId", description = "源码 ID", required = true)
    @Parameter(name = "dto", description = "拉取请求", required = true)
    @RequiresPermission("agent:execute")
    public Response pull(
            @PathParam("sourceId") UUID sourceId,
            @Valid AgentSourceVersionRecord.PullRequest dto) {
        AgentSourceVersionRecord.PullRequest pullDto = new AgentSourceVersionRecord.PullRequest(
                sourceId, dto.version(), dto.useCache(), dto.pulledBy()
        );
        return Response.ok(versionService.pull(pullDto)).build();
    }

    @DELETE
    @Path("/cache/{cacheId}")
    @Operation(summary = "使单个缓存失效")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功使缓存失效"),
        @APIResponse(responseCode = "404", description = "缓存不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "cacheId", description = "缓存 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response invalidateCache(@PathParam("cacheId") UUID cacheId) {
        versionService.invalidateCache(cacheId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{sourceId}/cache")
    @Operation(summary = "清空源码的所有缓存")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功清空缓存"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "sourceId", description = "源码 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response clearCache(@PathParam("sourceId") UUID sourceId) {
        versionService.clearCache(sourceId);
        return Response.noContent().build();
    }
}
