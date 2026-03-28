package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.ExecutionStatus;
import com.easystation.agent.record.CommandExecutionRecord;
import com.easystation.agent.service.CommandExecutionService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/agent-commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandExecutionResource {

    @Inject
    CommandExecutionService commandExecutionService;

    /**
     * Execute a command on an agent instance.
     * POST /api/v1/agent-commands/execute
     */
    @POST
    @Path("/execute")
    @RequiresPermission("agent:execute")
    public Response execute(
            @Valid CommandExecutionRecord.ExecuteRequest request,
            @Context SecurityContext securityContext,
            @HeaderParam("X-Client-IP") String clientIp) {
        String username = securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : "system";
        CommandExecutionRecord.ExecuteResponse response = commandExecutionService.execute(request, username, clientIp);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Get execution status by ID.
     * GET /api/v1/agent-commands/{executionId}/status
     */
    @GET
    @Path("/{executionId}/status")
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getStatus(@PathParam("executionId") UUID executionId) {
        return commandExecutionService.getStatus(executionId);
    }

    /**
     * Get execution detail by ID.
     * GET /api/v1/agent-commands/{executionId}
     */
    @GET
    @Path("/{executionId}")
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getDetail(@PathParam("executionId") UUID executionId) {
        return commandExecutionService.getStatus(executionId);
    }

    /**
     * Retry a failed execution.
     * POST /api/v1/agent-commands/{executionId}/retry
     */
    @POST
    @Path("/{executionId}/retry")
    @RequiresPermission("agent:execute")
    public CommandExecutionRecord.ExecuteResponse retry(
            @PathParam("executionId") UUID executionId,
            @Context SecurityContext securityContext,
            @HeaderParam("X-Client-IP") String clientIp) {
        String username = securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : "system";
        return commandExecutionService.retry(executionId, username, clientIp);
    }

    /**
     * List execution history with pagination and filtering.
     * GET /api/v1/agent-commands
     */
    @GET
    @RequiresPermission("agent:view")
    public Response list(
            @QueryParam("agentInstanceId") UUID agentInstanceId,
            @QueryParam("templateId") UUID templateId,
            @QueryParam("status") ExecutionStatus status,
            @QueryParam("executedBy") String executedBy,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {

        LocalDateTime startDateTime = parseDateTime(startTime);
        LocalDateTime endDateTime = parseDateTime(endTime);

        List<CommandExecutionRecord.ListResponse> executions = commandExecutionService.list(
                agentInstanceId, templateId, status, executedBy, startDateTime, endDateTime, page, size);

        long total = commandExecutionService.count(
                agentInstanceId, templateId, status, executedBy, startDateTime, endDateTime);

        return Response.ok()
                .entity(Map.of(
                        "data", executions,
                        "total", total,
                        "page", page != null ? page : 0,
                        "size", size != null ? size : 20
                ))
                .build();
    }

    /**
     * Cancel a running execution.
     * POST /api/v1/agent-commands/{executionId}/cancel
     */
    @POST
    @Path("/{executionId}/cancel")
    @RequiresPermission("agent:execute")
    public Response cancel(@PathParam("executionId") UUID executionId) {
        // TODO: Implement cancel functionality if needed
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(Map.of("message", "Cancel functionality not implemented"))
                .build();
    }


    /**
     * Agent callback - receive execution result from agent.
     * POST /api/v1/agent-commands/{executionId}/callback
     * 
     * @param executionId 执行 ID
     * @param request 回调请求（状态、退出码、输出、耗时）
     * @return 回调响应
     */
    @POST
    @Path(/{executionId}/callback)
    @Operation(summary = "Agent 任务执行结果回调", description = "接收 Agent 上报的任务执行结果，更新执行状态")
    public Response callback(
            @Parameter(description = "执行 ID") @PathParam("executionId") UUID executionId,
            @Valid CommandExecutionRecord.CallbackRequest request) {
        try {
            commandExecutionService.handleExecutionResult(
                    executionId,
                    request.status(),
                    request.exitCode(),
                    request.durationMs(),
                    request.output()
            );
            return Response.ok(new CommandExecutionRecord.CallbackResponse(true, "Callback received")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CommandExecutionRecord.CallbackResponse(false, e.getMessage()))
                    .build();
        }
    }
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            // Try parsing as ISO date with time
            try {
                return LocalDateTime.parse(dateTimeStr + "T00:00:00");
            } catch (Exception ex) {
                return null;
            }
        }
    }
}