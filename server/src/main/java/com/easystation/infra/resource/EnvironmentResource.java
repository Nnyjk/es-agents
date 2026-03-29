package com.easystation.infra.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.infra.record.EnvironmentRecord;
import com.easystation.infra.service.EnvironmentService;
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

@Path("/api/v1/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "环境管理", description = "部署环境配置管理 API")
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @GET
    @Operation(summary = "查询环境列表", description = "获取所有部署环境")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回环境列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("environment:view")
    public Response list() {
        return Response.ok(environmentService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取环境详情", description = "获取指定环境详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回环境详情"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "环境 ID", required = true)
    @RequiresPermission("environment:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(environmentService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建环境", description = "创建新的部署环境")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "环境创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("environment:create")
    public Response create(@Valid EnvironmentRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(environmentService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新环境", description = "更新现有环境配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "环境更新成功"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "环境 ID", required = true)
    @RequiresPermission("environment:edit")
    public Response update(@PathParam("id") UUID id, @Valid EnvironmentRecord.Update dto) {
        return Response.ok(environmentService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除环境", description = "删除指定部署环境")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "环境删除成功"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "环境 ID", required = true)
    @RequiresPermission("environment:delete")
    public Response delete(@PathParam("id") UUID id) {
        environmentService.delete(id);
        return Response.noContent().build();
    }
}
