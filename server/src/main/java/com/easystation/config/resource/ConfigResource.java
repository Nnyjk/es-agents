package com.easystation.config.resource;

import com.easystation.config.dto.ConfigRecord;
import com.easystation.config.service.ConfigService;
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

import java.util.List;
import java.util.UUID;

@Path("/configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "配置管理", description = "系统配置管理 API")
public class ConfigResource {

    @Inject
    ConfigService configService;

    @GET
    @Operation(summary = "获取配置列表", description = "分页查询配置列表，支持按关键字、环境、分组、状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置列表")
    })
    public Response list(
            @Parameter(description = "配置键（模糊匹配）") @QueryParam("key") String key,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "配置分组") @QueryParam("group") String group,
            @Parameter(description = "是否启用") @QueryParam("active") Boolean active,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        ConfigRecord.Query query = new ConfigRecord.Query(key, environmentId, group, active, limit, offset);
        return Response.ok(configService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取配置详情", description = "根据 ID 查询配置详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置详情"),
        @APIResponse(responseCode = "404", description = "配置不存在")
    })
    public Response get(@Parameter(description = "配置 ID") @PathParam("id") UUID id) {
        return Response.ok(configService.get(id)).build();
    }

    @GET
    @Path("/key/{key}")
    @Operation(summary = "根据键获取配置", description = "根据配置键查询配置值")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置"),
        @APIResponse(responseCode = "404", description = "配置不存在")
    })
    public Response getByKey(
            @Parameter(description = "配置键") @PathParam("key") String key,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId) {
        return Response.ok(configService.getByKey(key, environmentId)).build();
    }

    @POST
    @Operation(summary = "创建配置", description = "创建新的配置项")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建配置"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response create(@Valid ConfigRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(configService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新配置", description = "更新指定配置的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新配置"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "配置不存在")
    })
    public Response update(@Parameter(description = "配置 ID") @PathParam("id") UUID id, @Valid ConfigRecord.Update dto) {
        return Response.ok(configService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除配置", description = "删除指定的配置项")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除配置"),
        @APIResponse(responseCode = "404", description = "配置不存在")
    })
    public Response delete(@Parameter(description = "配置 ID") @PathParam("id") UUID id, @Parameter(description = "删除操作者") @QueryParam("deletedBy") String deletedBy) {
        configService.delete(id, deletedBy);
        return Response.noContent().build();
    }

    @POST
    @Path("/batch")
    @Operation(summary = "批量更新配置", description = "批量更新多个配置项")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功批量更新配置"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response batchUpdate(@Valid ConfigRecord.BatchUpdate dto) {
        configService.batchUpdate(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/history")
    @Operation(summary = "获取配置变更历史", description = "查询指定配置的变更历史")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回变更历史"),
        @APIResponse(responseCode = "404", description = "配置不存在")
    })
    public Response getHistory(
            @Parameter(description = "配置 ID") @PathParam("id") UUID configId,
            @Parameter(description = "配置键") @QueryParam("key") String key,
            @Parameter(description = "变更类型") @QueryParam("changeType") String changeType,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        ConfigRecord.HistoryQuery query = new ConfigRecord.HistoryQuery(configId, key, changeType, limit, offset);
        return Response.ok(configService.getHistory(query)).build();
    }

    @GET
    @Path("/history")
    @Operation(summary = "获取所有配置变更历史", description = "查询所有配置的变更历史")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回变更历史")
    })
    public Response getAllHistory(
            @Parameter(description = "配置 ID") @QueryParam("configId") UUID configId,
            @Parameter(description = "配置键") @QueryParam("key") String key,
            @Parameter(description = "变更类型") @QueryParam("changeType") String changeType,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        ConfigRecord.HistoryQuery query = new ConfigRecord.HistoryQuery(configId, key, changeType, limit, offset);
        return Response.ok(configService.getHistory(query)).build();
    }

    @POST
    @Path("/history/{historyId}/rollback")
    @Operation(summary = "回滚配置", description = "将配置回滚到指定的历史版本")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功回滚配置"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "历史记录不存在")
    })
    public Response rollback(@Parameter(description = "历史记录 ID") @PathParam("historyId") UUID historyId, @Valid ConfigRecord.RollbackRequest dto) {
        return Response.ok(configService.rollback(historyId, dto)).build();
    }

    @POST
    @Path("/environments")
    @Operation(summary = "按环境获取配置", description = "批量获取多个环境的配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response getByEnvironments(List<UUID> environmentIds) {
        return Response.ok(configService.getByEnvironment(environmentIds)).build();
    }

    @GET
    @Path("/diff")
    @Operation(summary = "对比环境配置差异", description = "对比两个环境的配置差异")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置差异"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response diff(
            @Parameter(description = "环境 1 ID") @QueryParam("envId1") UUID envId1,
            @Parameter(description = "环境 2 ID") @QueryParam("envId2") UUID envId2) {
        return Response.ok(configService.diff(envId1, envId2)).build();
    }

    @GET
    @Path("/groups")
    @Operation(summary = "获取配置分组列表", description = "获取所有配置分组")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置分组列表")
    })
    public Response listGroups() {
        return Response.ok(configService.listGroups()).build();
    }
}