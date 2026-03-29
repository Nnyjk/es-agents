package com.easystation.agent.tool.resource;

import com.easystation.agent.tool.domain.ToolExecutionLog;
import com.easystation.agent.tool.domain.ToolExecutionStatus;
import com.easystation.agent.tool.dto.ToolExecutionRequest;
import com.easystation.agent.tool.dto.ToolExecutionResponse;
import com.easystation.agent.tool.repository.ToolExecutionLogRepository;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import com.easystation.agent.tool.spi.ToolExecutor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工具执行 REST API
 */
@Path("/api/agent/tools/execute")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ToolExecutionResource {

    @Inject
    ToolExecutor toolExecutor;

    @Inject
    ToolExecutionLogRepository executionLogRepository;

    /**
     * 同步执行工具
     */
    @POST
    public ToolExecutionResponse execute(ToolExecutionRequest request) {
        if (request.getToolId() == null || request.getToolId().trim().isEmpty()) {
            throw new BadRequestException("toolId is required");
        }

        ToolExecutionResult result = toolExecutor.execute(
                request.getToolId(),
                request.getParameters() != null ? request.getParameters() : java.util.Collections.emptyMap(),
                request.getTimeout() != null ? request.getTimeout() : 30000L
        );

        return ToolExecutionResponse.builder()
                .executionId(UUID.randomUUID())
                .status(result.getStatus())
                .output(result.getOutput())
                .error(result.getError())
                .durationMs(result.getDurationMs())
                .executedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 异步执行工具
     */
    @POST
    @Path("/async")
    public Response executeAsync(ToolExecutionRequest request) {
        if (request.getToolId() == null || request.getToolId().trim().isEmpty()) {
            throw new BadRequestException("toolId is required");
        }

        String executionId = UUID.randomUUID().toString();

        // 启动异步执行
        toolExecutor.executeAsync(
                request.getToolId(),
                request.getParameters() != null ? request.getParameters() : java.util.Collections.emptyMap(),
                request.getTimeout() != null ? request.getTimeout() : 30000L
        );

        return Response.accepted()
                .entity(Map.of("executionId", executionId, "status", "PENDING"))
                .build();
    }

    /**
     * 取消执行
     */
    @POST
    @Path("/{executionId}/cancel")
    public Response cancelExecution(@RestPath String executionId) {
        boolean cancelled = toolExecutor.cancelExecution(executionId);
        if (cancelled) {
            return Response.ok(Map.of("status", "CANCELLED")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Execution not found or already completed"))
                    .build();
        }
    }

    /**
     * 检查执行状态
     */
    @GET
    @Path("/{executionId}/status")
    public Response getExecutionStatus(@RestPath String executionId) {
        boolean running = toolExecutor.isRunning(executionId);
        return Response.ok(Map.of("executionId", executionId, "running", running)).build();
    }

    /**
     * 列出执行历史
     */
    @GET
    @Path("/history")
    public List<ToolExecutionResponse> getExecutionHistory(
            @QueryParam("toolId") String toolId,
            @QueryParam("taskId") String taskId,
            @QueryParam("limit") @DefaultValue("50") int limit) {

        List<ToolExecutionLog> logs;
        if (toolId != null && !toolId.isEmpty()) {
            logs = executionLogRepository.findByToolId(toolId, limit);
        } else if (taskId != null && !taskId.isEmpty()) {
            logs = executionLogRepository.findByTaskId(taskId);
        } else {
            logs = executionLogRepository.findAll()
                    .page(0, limit)
                    .list();
        }

        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取执行日志详情
     */
    @GET
    @Path("/history/{id}")
    public ToolExecutionResponse getExecutionLog(@RestPath UUID id) {
        ToolExecutionLog log = executionLogRepository.findById(id);
        if (log == null) {
            throw new NotFoundException("Execution log not found: " + id);
        }
        return toResponse(log);
    }

    /**
     * 查找失败的执行
     */
    @GET
    @Path("/history/failed")
    public List<ToolExecutionResponse> getFailedExecutions(
            @QueryParam("toolId") String toolId,
            @QueryParam("limit") @DefaultValue("50") int limit) {

        List<ToolExecutionLog> logs;
        if (toolId != null && !toolId.isEmpty()) {
            logs = executionLogRepository.findFailedByToolId(toolId);
        } else {
            logs = executionLogRepository.findFailed();
        }

        return logs.stream()
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 清理旧日志
     */
    @DELETE
    @Path("/history/cleanup")
    public Response cleanupHistory(@QueryParam("before") String before) {
        if (before == null || before.isEmpty()) {
            throw new BadRequestException("Parameter 'before' is required (ISO date format)");
        }

        try {
            LocalDateTime beforeDate = LocalDateTime.parse(before);
            executionLogRepository.cleanupBefore(beforeDate);
            return Response.ok(Map.of("status", "CLEANED", "before", before)).build();
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format: " + before);
        }
    }

    /**
     * 转换为响应 DTO
     */
    private ToolExecutionResponse toResponse(ToolExecutionLog log) {
        return ToolExecutionResponse.builder()
                .executionId(log.id)
                .status(log.status)
                .output(log.output)
                .error(log.error)
                .durationMs(log.durationMs)
                .executedAt(log.executedAt)
                .build();
    }
}
