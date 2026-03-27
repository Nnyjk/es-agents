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

import java.util.UUID;

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
     * 获取指定类型的打包配置
     */
    @GET
    @Path("/type/{type}")
    @RequiresPermission("agent:view")
    public Response listByType(@PathParam("type") PackageType type) {
        return Response.ok(service.listByType(type)).build();
    }

    /**
     * 获取单个打包配置
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
        return Response.status(Response.Status.CREATED)
            .entity(created)
            .build();
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
     * 导出打包配置
     */
    @GET
    @Path("/{id}/export")
    @RequiresPermission("agent:view")
    public Response export(@PathParam("id") UUID id) {
        return Response.ok(service.export(id))
            .header("Content-Disposition", "attachment; filename=package-config.json")
            .build();
    }

    /**
     * 导入打包配置
     */
    @POST
    @Path("/import")
    @RequiresPermission("agent:create")
    public Response importConfig(@Valid PackageConfigRecord.Import record) {
        PackageConfigRecord imported = service.importConfig(record);
        return Response.status(Response.Status.CREATED)
            .entity(imported)
            .build();
    }
}