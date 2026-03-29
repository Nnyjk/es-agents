package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentLogRecord;
import com.easystation.agent.dto.AgentLogRecord.DeploymentLogQuery;
import com.easystation.agent.dto.AgentLogRecord.CommandLogQuery;
import com.easystation.agent.dto.AgentLogRecord.TaskLogRecord;
import com.easystation.agent.service.AgentLogService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Path("/agents/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentLogResource {

    @Inject
    AgentLogService agentLogService;

    @GET
    @Path("/{agentId}")
    @RequiresPermission("agent:view")
    public Response queryLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("level") String level,
            @QueryParam("keyword") String keyword,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        var query = new AgentLogRecord.Query(
            agentId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            level,
            keyword,
            null, // TODO: parse startTime
            null  // TODO: parse endTime
        );
        
        var result = agentLogService.queryLogs(query);
        return Response.ok(result).build();
    }

    @GET
    @RequiresPermission("agent:view")
    @Path("/{agentId}/stats")
    public Response getLogStats(@PathParam("agentId") UUID agentId) {
        var stats = agentLogService.getLogStats(agentId);
        return Response.ok(stats).build();
    }

    @GET
    @RequiresPermission("agent:view")
    @Path("/{agentId}/tail")
    public Response tailLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("lines") Integer lines) {
        
        var logs = agentLogService.getLatestLogs(agentId, lines != null ? lines : 50);
        return Response.ok(logs).build();
    }

    /**
     * 获取部署过程日志
     */
    @GET
    @RequiresPermission("agent:view")
    @Path("/{agentId}/deployment")
    public Response getDeploymentLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("deploymentId") UUID deploymentId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        LocalDateTime start = startTime != null ? parseDateTime(startTime) : null;
        LocalDateTime end = endTime != null ? parseDateTime(endTime) : null;
        
        var query = new AgentLogRecord.DeploymentLogQuery(
            agentId,
            deploymentId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            start,
            end
        );
        
        var result = agentLogService.queryDeploymentLogs(query);
        return Response.ok(result).build();
    }

    /**
     * 获取命令执行日志
     */
    @GET
    @RequiresPermission("agent:view")
    @Path("/{agentId}/command/{executionId}")
    public Response getCommandLogs(
            @PathParam("agentId") UUID agentId,
            @PathParam("executionId") UUID executionId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        LocalDateTime start = startTime != null ? parseDateTime(startTime) : null;
        LocalDateTime end = endTime != null ? parseDateTime(endTime) : null;
        
        var query = new AgentLogRecord.CommandLogQuery(
            agentId,
            executionId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            start,
            end
        );
        
        var result = agentLogService.queryCommandLogs(query);
        return Response.ok(result).build();
    }

    /**
     * 获取特定任务的执行日志
     */
    @GET
    @RequiresPermission("agent:view")
    @Path("/tasks/{taskId}")
    public Response getTaskLog(@PathParam("taskId") UUID taskId) {
        var result = agentLogService.getTaskLog(taskId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Task not found: " + taskId)
                .build();
        }
        return Response.ok(result).build();
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            // 尝试其他格式
            try {
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}