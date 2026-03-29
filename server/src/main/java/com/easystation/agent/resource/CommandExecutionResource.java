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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/agent-commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "命令执行管理", description = "Agent 命令执行与任务管理 API")
public class CommandExecutionResource {

    @Inject
    CommandExecutionService commandExecutionService;

    @POST
    @Path("/execute")
    @Operation(summary = "执行命令", description = "在 Agent 实例上执行命令")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "命令执行已创建"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "X-Client-IP", description = "客户端 IP 地址", required = false)
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

    @GET
    @Path("/{executionId}/status")
    @Operation(summary = "获取执行状态", description = "获取命令执行状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行状态"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getStatus(@PathParam("executionId") UUID executionId) {
        return commandExecutionService.getStatus(executionId);
    }

    @GET
    @Path("/{executionId}")
    @Operation(summary = "获取执行详情", description = "获取命令执行详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:view")
    public CommandExecutionRecord.DetailResponse getDetail(@PathParam("executionId") UUID executionId) {
        return commandExecutionService.getStatus(executionId);
    }

    @POST
    @Path("/{executionId}/retry")
    @Operation(summary = "重试执行", description = "重试失败的命令执行")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "重试已创建"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @Parameter(name = "X-Client-IP", description = "客户端 IP 地址", required = false)
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

    @GET
    @Operation(summary = "查询执行历史", description = "分页查询命令执行历史")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行历史列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentInstanceId", description = "按 Agent 实例 ID 过滤", required = false)
    @Parameter(name = "templateId", description = "按模板 ID 过滤", required = false)
    @Parameter(name = "status", description = "按执行状态过滤", required = false)
    @Parameter(name = "executedBy", description = "按执行人过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "page", description = "页码", required = false)
    @Parameter(name = "size", description = "每页数量", required = false)
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

    @POST
    @Path("/{executionId}/cancel")
    @Operation(summary = "取消执行", description = "取消正在运行的命令执行")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "取消成功"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "501", description = "功能未实现"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response cancel(@PathParam("executionId") UUID executionId) {
        // TODO: Implement cancel functionality if needed
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(Map.of("message", "Cancel functionality not implemented"))
                .build();
    }

    @POST
    @Path("/{executionId}/callback")
    @Operation(summary = "执行回调", description = "Agent 回调上报执行结果")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "回调接收成功"),
        @APIResponse(responseCode = "400", description = "回调参数无效"),
        @APIResponse(responseCode = "404", description = "执行记录不存在")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    public Response callback(
            @PathParam("executionId") UUID executionId,
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
