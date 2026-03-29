package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.CommandCategory;
import com.easystation.agent.record.CommandExecutionRecord;
import com.easystation.agent.record.CommandTemplateRecord;
import com.easystation.agent.service.CommandTemplateService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/commands/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "命令模板管理", description = "命令模板配置与执行 API")
public class CommandTemplateResource {

    @Inject
    CommandTemplateService commandTemplateService;

    @GET
    @Operation(summary = "查询模板列表", description = "查询所有命令模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回模板列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "category", description = "按分类过滤", required = false)
    @Parameter(name = "activeOnly", description = "仅查询启用状态", required = false)
    @RequiresPermission("agent:view")
    public List<CommandTemplateRecord.ListResponse> list(
            @QueryParam("category") CommandCategory category,
            @QueryParam("activeOnly") Boolean activeOnly) {
        return commandTemplateService.list(category, activeOnly);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取模板详情", description = "获取指定命令模板详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回模板详情"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:view")
    public CommandTemplateRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return commandTemplateService.getById(id);
    }

    @POST
    @Operation(summary = "创建模板", description = "创建新的命令模板")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "模板创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(
            @Valid CommandTemplateRecord.CreateRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        CommandTemplateRecord.DetailResponse response = commandTemplateService.create(request, username);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新模板", description = "更新现有命令模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "模板更新成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:edit")
    public CommandTemplateRecord.DetailResponse update(
            @PathParam("id") UUID id,
            @Valid CommandTemplateRecord.UpdateRequest request) {
        return commandTemplateService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除模板", description = "删除指定命令模板")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "模板删除成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        commandTemplateService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/execute")
    @Operation(summary = "执行模板命令", description = "在 Agent 实例上执行命令模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "命令执行已创建"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:execute")
    public CommandTemplateRecord.ExecuteResponse execute(
            @PathParam("id") UUID id,
            @Valid CommandTemplateRecord.ExecuteRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return commandTemplateService.execute(id, request, username);
    }

    @GET
    @Path("/{id}/executions")
    @Operation(summary = "查询执行历史", description = "获取模板的执行历史记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行历史"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:view")
    public List<CommandExecutionRecord.ListResponse> getExecutionHistory(@PathParam("id") UUID id) {
        return commandTemplateService.getExecutionHistory(id);
    }

    @GET
    @Path("/executions/{executionId}")
    @Operation(summary = "获取执行详情", description = "获取指定执行记录详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getExecutionById(@PathParam("executionId") UUID executionId) {
        return commandTemplateService.getExecutionById(executionId);
    }
}
