package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.agent.dto.AgentStateTransitionRecord;
import com.easystation.agent.dto.AgentStateTransitionRecord.AvailableTransitions;
import com.easystation.agent.dto.AgentStateTransitionRecord.StateHistory;
import com.easystation.agent.dto.AgentStateTransitionRecord.TransitionRequest;
import com.easystation.agent.dto.AgentStateTransitionRecord.TransitionResult;
import com.easystation.agent.service.AgentStateMachineService;
import com.easystation.auth.annotation.RequiresPermission;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/v1/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 状态管理", description = "Agent 状态机流转、历史查询 API")
public class AgentStateResource {

    @Inject
    AgentStateMachineService stateMachineService;

    @POST
    @Path("/{id}/transition")
    @Operation(summary = "执行状态流转（带校验）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态流转成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效或状态流转不合法"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response transition(
            @PathParam("id") UUID id,
            @Valid TransitionRequest request) {
        TransitionResult result = stateMachineService.transition(id, request);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}/transitions")
    @Operation(summary = "获取当前状态可用的流转选项")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回可用流转列表"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getAvailableTransitions(@PathParam("id") UUID id) {
        AvailableTransitions transitions = stateMachineService.getAvailableTransitions(id);
        return Response.ok(transitions).build();
    }

    @GET
    @Path("/{id}/state-history")
    @Operation(summary = "获取状态变更历史", description = "支持分页查询")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回状态历史"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @Parameter(name = "page", description = "页码（从 0 开始，默认 0）", required = false)
    @Parameter(name = "size", description = "每页数量（默认 20）", required = false)
    @RequiresPermission("agent:view")
    public Response getStateHistory(
            @PathParam("id") UUID id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        StateHistory history = stateMachineService.getStateHistory(id, page, size);
        return Response.ok(history).build();
    }

    @POST
    @Path("/batch-transition")
    @Operation(summary = "批量状态流转")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "批量流转成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效或未提供实例 ID 列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "ids", description = "Agent 实例 ID 列表（逗号分隔）", required = true)
    @RequiresPermission("agent:execute")
    public Response batchTransition(
            @QueryParam("ids") List<UUID> instanceIds,
            @Valid TransitionRequest request) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No instance IDs provided")
                    .build();
        }
        List<TransitionResult> results = stateMachineService.batchTransition(instanceIds, request);
        return Response.ok(results).build();
    }

    @POST
    @Path("/{id}/force-transition")
    @Operation(summary = "强制状态流转（管理员操作，跳过校验）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "强制流转成功"),
        @APIResponse(responseCode = "400", description = "目标状态未指定"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @Parameter(name = "targetStatus", description = "目标状态", required = true)
    @Parameter(name = "reason", description = "流转原因", required = false)
    @Parameter(name = "operator", description = "操作人（默认 admin）", required = false)
    @RequiresPermission("agent:admin")
    public Response forceTransition(
            @PathParam("id") UUID id,
            @QueryParam("targetStatus") AgentStatus targetStatus,
            @QueryParam("reason") String reason,
            @QueryParam("operator") @DefaultValue("admin") String operator) {
        if (targetStatus == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Target status is required")
                    .build();
        }
        TransitionResult result = stateMachineService.forceTransition(id, targetStatus, reason, operator);
        return Response.ok(result).build();
    }

    @GET
    @Path("/validate-transition")
    @Operation(summary = "校验状态流转是否合法（不执行流转）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回校验结果"),
        @APIResponse(responseCode = "400", description = "缺少必需参数"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "from", description = "源状态", required = true)
    @Parameter(name = "to", description = "目标状态", required = true)
    @RequiresPermission("agent:view")
    public Response validateTransition(
            @QueryParam("from") AgentStatus fromStatus,
            @QueryParam("to") AgentStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both from and to status are required")
                    .build();
        }
        boolean allowed = stateMachineService.isTransitionAllowed(fromStatus, toStatus);
        return Response.ok(Map.of(
                "fromStatus", fromStatus,
                "toStatus", toStatus,
                "allowed", allowed,
                "message", allowed ?
                        "Transition is valid" :
                        "Transition is not allowed from " + fromStatus + " to " + toStatus
        )).build();
    }

    @GET
    @Path("/state-rules")
    @Operation(summary = "获取所有状态流转规则")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回状态流转规则概览"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("agent:view")
    public Response getStateRules() {
        // 返回状态流转规则概览
        return Response.ok(List.of(
                Map.of("status", "UNCONFIGURED", "allowedTargets", List.of("PREPARING", "ERROR"), "description", "初始状态，需配置"),
                Map.of("status", "PREPARING", "allowedTargets", List.of("READY", "ERROR", "UNCONFIGURED"), "description", "准备中，配置完成后进入 READY"),
                Map.of("status", "READY", "allowedTargets", List.of("PACKAGING", "DEPLOYING", "ERROR"), "description", "就绪，可打包或部署"),
                Map.of("status", "PACKAGING", "allowedTargets", List.of("PACKAGED", "ERROR", "READY"), "description", "打包中"),
                Map.of("status", "PACKAGED", "allowedTargets", List.of("DEPLOYING", "ERROR", "READY"), "description", "已打包，可部署"),
                Map.of("status", "DEPLOYING", "allowedTargets", List.of("DEPLOYED", "ERROR", "READY"), "description", "部署中"),
                Map.of("status", "DEPLOYED", "allowedTargets", List.of("ONLINE", "ERROR", "OFFLINE"), "description", "已部署，等待上线"),
                Map.of("status", "ONLINE", "allowedTargets", List.of("OFFLINE", "ERROR"), "description", "在线运行"),
                Map.of("status", "OFFLINE", "allowedTargets", List.of("ONLINE", "ERROR", "READY"), "description", "离线"),
                Map.of("status", "ERROR", "allowedTargets", List.of("READY", "UNCONFIGURED"), "description", "错误状态，可恢复")
        )).build();
    }
}
