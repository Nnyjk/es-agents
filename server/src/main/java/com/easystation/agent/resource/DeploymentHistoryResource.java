package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.DeploymentStatus;
import com.easystation.agent.record.DeploymentHistoryRecord;
import com.easystation.agent.service.DeploymentHistoryService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/api/v1/deployments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "部署历史管理", description = "Agent 部署历史记录与回滚 API")
public class DeploymentHistoryResource {

    @Inject
    DeploymentHistoryService deploymentHistoryService;

    @GET
    @Path("/history")
    @Operation(summary = "查询部署历史", description = "获取 Agent 实例的部署历史记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回部署历史列表"),
        @APIResponse(responseCode = "400", description = "缺少 agentInstanceId 参数"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentInstanceId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public List<DeploymentHistoryRecord.ListResponse> getHistory(
            @QueryParam("agentInstanceId") UUID agentInstanceId) {
        if (agentInstanceId == null) {
            throw new WebApplicationException("agentInstanceId is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.getHistoryByAgentInstance(agentInstanceId);
    }

    @GET
    @Path("/history/{id}")
    @Operation(summary = "获取部署详情", description = "获取指定部署记录详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回部署详情"),
        @APIResponse(responseCode = "404", description = "部署记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "部署记录 ID", required = true)
    @RequiresPermission("agent:view")
    public DeploymentHistoryRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return deploymentHistoryService.getById(id);
    }

    @POST
    @Path("/history")
    @Operation(summary = "创建部署记录", description = "创建新的部署历史记录")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "部署记录创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(
            DeploymentHistoryRecord.CreateRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        DeploymentHistoryRecord.DetailResponse response = deploymentHistoryService.create(request, username);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/history/{id}/status")
    @Operation(summary = "更新部署状态", description = "更新部署记录的状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态更新成功"),
        @APIResponse(responseCode = "404", description = "部署记录不存在"),
        @APIResponse(responseCode = "400", description = "缺少 status 参数"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "部署记录 ID", required = true)
    @Parameter(name = "status", description = "部署状态", required = true)
    @RequiresPermission("agent:edit")
    public DeploymentHistoryRecord.DetailResponse updateStatus(
            @PathParam("id") UUID id,
            @QueryParam("status") DeploymentStatus status) {
        if (status == null) {
            throw new WebApplicationException("status is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.updateStatus(id, status);
    }

    @POST
    @Path("/history/{id}/rollback")
    @Operation(summary = "回滚部署", description = "回滚到指定部署版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "回滚成功"),
        @APIResponse(responseCode = "404", description = "部署记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "部署记录 ID", required = true)
    @RequiresPermission("agent:execute")
    public DeploymentHistoryRecord.RollbackResponse rollback(
            @PathParam("id") UUID id,
            DeploymentHistoryRecord.RollbackRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return deploymentHistoryService.rollback(id, request != null ? request.reason() : null, username);
    }

    @DELETE
    @Path("/history/{id}")
    @Operation(summary = "删除部署记录", description = "删除指定部署历史记录")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "部署记录删除成功"),
        @APIResponse(responseCode = "404", description = "部署记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "部署记录 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        deploymentHistoryService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/latest")
    @Operation(summary = "获取最新成功部署", description = "获取 Agent 实例最新成功的部署记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回最新部署记录"),
        @APIResponse(responseCode = "400", description = "缺少 agentInstanceId 参数"),
        @APIResponse(responseCode = "404", description = "无成功部署记录"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentInstanceId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public DeploymentHistoryRecord.DetailResponse getLatestSuccessful(
            @QueryParam("agentInstanceId") UUID agentInstanceId) {
        if (agentInstanceId == null) {
            throw new WebApplicationException("agentInstanceId is required", Response.Status.BAD_REQUEST);
        }
        return deploymentHistoryService.getLatestSuccessful(agentInstanceId);
    }
}
