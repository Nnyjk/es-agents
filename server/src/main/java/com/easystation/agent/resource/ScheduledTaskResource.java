package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.ScheduledTaskCategory;
import com.easystation.agent.record.ScheduledTaskExecutionRecord;
import com.easystation.agent.record.ScheduledTaskRecord;
import com.easystation.agent.service.ScheduledTaskService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

/**
 * 定时任务 REST API
 * 提供定时任务的管理、调度、执行等功能
 */
@Path("/scheduled-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduledTaskResource {

    @Inject
    ScheduledTaskService scheduledTaskService;

    /**
     * 查询定时任务列表
     * GET /scheduled-tasks?category={category}&activeOnly={true/false}
     */
    @GET
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<ScheduledTaskRecord.ListResponse> list(
            @QueryParam("category") ScheduledTaskCategory category,
            @QueryParam("activeOnly") Boolean activeOnly
    ) {
        return scheduledTaskService.list(category, activeOnly);
    }

    /**
     * 查询定时任务详情
     * GET /scheduled-tasks/{id}
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public ScheduledTaskRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return scheduledTaskService.getById(id);
    }

    /**
     * 创建定时任务
     * POST /scheduled-tasks
     */
    @POST
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:create")
    public Response create(@Valid ScheduledTaskRecord.CreateRequest request) {
        ScheduledTaskRecord.DetailResponse task = scheduledTaskService.create(request);
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    /**
     * 更新定时任务
     * PUT /scheduled-tasks/{id}
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:edit")
    public ScheduledTaskRecord.DetailResponse update(
            @PathParam("id") UUID id,
            @Valid ScheduledTaskRecord.UpdateRequest request
    ) {
        return scheduledTaskService.update(id, request);
    }

    /**
     * 删除定时任务
     * DELETE /scheduled-tasks/{id}
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"Admin"})
    @RequiresPermission("agent:delete")
    public void delete(@PathParam("id") UUID id) {
        scheduledTaskService.delete(id);
    }

    /**
     * 启用定时任务
     * POST /scheduled-tasks/{id}/enable
     */
    @POST
    @Path("/{id}/enable")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public ScheduledTaskRecord.DetailResponse enable(@PathParam("id") UUID id) {
        return scheduledTaskService.enable(id);
    }

    /**
     * 禁用定时任务
     * POST /scheduled-tasks/{id}/disable
     */
    @POST
    @Path("/{id}/disable")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public ScheduledTaskRecord.DetailResponse disable(@PathParam("id") UUID id) {
        return scheduledTaskService.disable(id);
    }

    /**
     * 立即执行定时任务
     * POST /scheduled-tasks/{id}/execute
     */
    @POST
    @Path("/{id}/execute")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public ScheduledTaskExecutionRecord.DetailResponse executeNow(
            @PathParam("id") UUID id,
            @Context SecurityContext securityContext,
            @QueryParam("agentInstanceId") UUID agentInstanceId
    ) {
        // 如果没有指定 agentInstanceId，可以生成一个默认的或使用当前节点 ID
        if (agentInstanceId == null) {
            agentInstanceId = UUID.randomUUID();
        }
        return scheduledTaskService.executeNow(id, agentInstanceId);
    }

    /**
     * 查询任务执行历史
     * GET /scheduled-tasks/{id}/executions
     */
    @GET
    @Path("/{id}/executions")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<ScheduledTaskExecutionRecord.ListResponse> getExecutionHistory(@PathParam("id") UUID id) {
        return scheduledTaskService.getExecutionHistory(id);
    }

    /**
     * 查询执行记录详情
     * GET /scheduled-tasks/executions/{executionId}
     */
    @GET
    @Path("/executions/{executionId}")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public ScheduledTaskExecutionRecord.DetailResponse getExecutionById(@PathParam("executionId") UUID executionId) {
        return scheduledTaskService.getExecutionById(executionId);
    }

    /**
     * 更新执行状态
     * PUT /scheduled-tasks/executions/{executionId}/status
     */
    @PUT
    @Path("/executions/{executionId}/status")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:edit")
    public ScheduledTaskExecutionRecord.DetailResponse updateExecutionStatus(
            @PathParam("executionId") UUID executionId,
            @Valid ScheduledTaskExecutionRecord.UpdateStatusRequest request
    ) {
        return scheduledTaskService.updateExecutionStatus(executionId, request);
    }

    /**
     * 验证 Cron 表达式
     * POST /scheduled-tasks/validate-cron
     */
    @POST
    @Path("/validate-cron")
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public ScheduledTaskRecord.CronValidateResponse validateCron(
            @Valid ScheduledTaskRecord.CronValidateRequest request
    ) {
        boolean isValid = scheduledTaskService.isValidCronExpression(request.cronExpression());
        String message = isValid ? "Valid cron expression" : "Invalid cron expression";
        return new ScheduledTaskRecord.CronValidateResponse(isValid, message, null);
    }

    /**
     * 查询需要执行的任务
     * GET /scheduled-tasks/due
     */
    @GET
    @Path("/due")
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:view")
    public List<ScheduledTaskRecord.ListResponse> findDueTasks() {
        return scheduledTaskService.findDueTasks();
    }
}
