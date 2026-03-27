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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 打包配置 REST API
 */
@Path("/agents/package-configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PackageConfigResource {

    @Inject
    PackageConfigService service;

    /**
     * 获取所有打包配置
     */
    @GET
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(service.list()).build();
    }

    /**
     * 获取打包配置详情
     */
    @GET
    @Path("/{id}")
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(service.get(id)).build();
    }

    /**
     * 创建打包配置
     */
    @POST
    @RequiresPermission("agent:create")
    public Response create(@Valid PackageConfigRecord.Create record) {
        PackageConfigRecord created = service.create(record);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * 更新打包配置
     */
    @PUT
    @Path("/{id}")
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid PackageConfigRecord.Update record) {
        return Response.ok(service.update(id, record)).build();
    }

    /**
     * 删除打包配置（软删除）
     */
    @DELETE
    @Path("/{id}")
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    /**
     * 获取支持的打包类型列表
     */
    @GET
    @Path("/types")
    @RequiresPermission("agent:view")
    public Response getTypes() {
        List<String> types = Arrays.stream(PackageType.values())
            .map(Enum::name)
            .collect(Collectors.toList());
        return Response.ok(types).build();
    }

    /**
     * 按类型过滤打包配置列表
     */
    @GET
    @Path("/type/{type}")
    @RequiresPermission("agent:view")
    public Response listByType(@PathParam("type") PackageType type) {
        return Response.ok(service.listByType(type)).build();
    }

    /**
     * 启用/禁用打包配置
     */
    @PUT
    @Path("/{id}/enabled")
    @RequiresPermission("agent:edit")
    public Response setEnabled(@PathParam("id") UUID id, @QueryParam("enabled") boolean enabled) {
        return Response.ok(service.setEnabled(id, enabled)).build();
    }

}