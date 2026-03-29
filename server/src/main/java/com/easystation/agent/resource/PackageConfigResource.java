package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.PackageType;
import com.easystation.agent.dto.PackageConfigRecord;
import com.easystation.agent.service.PackageConfigService;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/v1/agents/package-configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "打包配置管理", description = "Agent 打包配置管理 API")
public class PackageConfigResource {

    @Inject
    PackageConfigService service;

    @GET
    @Operation(summary = "查询配置列表", description = "获取所有打包配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回配置列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(service.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取配置详情", description = "获取指定打包配置详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回配置详情"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "配置 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(service.get(id)).build();
    }

    @POST
    @Operation(summary = "创建配置", description = "创建新的打包配置")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "配置创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(@Valid PackageConfigRecord.Create record) {
        PackageConfigRecord created = service.create(record);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新配置", description = "更新现有打包配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "配置更新成功"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "配置 ID", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid PackageConfigRecord.Update record) {
        return Response.ok(service.update(id, record)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除配置", description = "删除打包配置 (软删除)")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "配置删除成功"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "配置 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/types")
    @Operation(summary = "获取打包类型", description = "获取所有支持的打包类型")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回类型列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public Response getTypes() {
        List<String> types = Arrays.stream(PackageType.values())
            .map(Enum::name)
            .collect(Collectors.toList());
        return Response.ok(types).build();
    }

    @GET
    @Path("/type/{type}")
    @Operation(summary = "按类型查询配置", description = "按打包类型过滤配置列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回配置列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "type", description = "打包类型", required = true)
    @RequiresPermission("agent:view")
    public Response listByType(@PathParam("type") PackageType type) {
        return Response.ok(service.listByType(type)).build();
    }

    @PUT
    @Path("/{id}/enabled")
    @Operation(summary = "启用/禁用配置", description = "设置打包配置的启用状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态更新成功"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "配置 ID", required = true)
    @Parameter(name = "enabled", description = "启用状态", required = true)
    @RequiresPermission("agent:edit")
    public Response setEnabled(@PathParam("id") UUID id, @QueryParam("enabled") boolean enabled) {
        return Response.ok(service.setEnabled(id, enabled)).build();
    }
}
