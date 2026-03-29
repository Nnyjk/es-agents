package com.easystation.notification.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.notification.dto.TemplateRecord;
import com.easystation.notification.service.NotificationTemplateService;
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

@Path("/api/v1/notification-templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "通知模板管理", description = "通知模板配置 API")
public class NotificationTemplateResource {

    @Inject
    NotificationTemplateService notificationTemplateService;

    @GET
    @Operation(summary = "列出通知模板", description = "获取所有通知模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知模板列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:view")
    public Response list() {
        return Response.ok(notificationTemplateService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取通知模板", description = "根据 ID 查询通知模板详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知模板"),
        @APIResponse(responseCode = "404", description = "通知模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知模板 ID", required = true)
    @RequiresPermission("notification:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(notificationTemplateService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建通知模板", description = "创建新的通知模板")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "通知模板创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("notification:create")
    public Response create(@Valid TemplateRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(notificationTemplateService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新通知模板", description = "更新通知模板内容")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "通知模板更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "通知模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知模板 ID", required = true)
    @RequiresPermission("notification:edit")
    public Response update(@PathParam("id") UUID id, @Valid TemplateRecord.Update dto) {
        return Response.ok(notificationTemplateService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除通知模板", description = "删除通知模板")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "通知模板删除成功"),
        @APIResponse(responseCode = "404", description = "通知模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "通知模板 ID", required = true)
    @RequiresPermission("notification:delete")
    public Response delete(@PathParam("id") UUID id) {
        notificationTemplateService.delete(id);
        return Response.noContent().build();
    }
}
