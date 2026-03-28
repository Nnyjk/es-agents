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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent 状态流转 API
 */
@Path("/v1/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentStateResource {

    @Inject
    AgentStateMachineService stateMachineService;

    /**
     * 状态流转（带校验）
     * POST /v1/agents/{id}/transition
     */
    @POST
    @Path("/{id}/transition")
    @RequiresPermission("agent:execute")
    public Response transition(
            @PathParam("id") UUID id,
            @Valid TransitionRequest request) {
        TransitionResult result = stateMachineService.transition(id, request);
        return Response.ok(result).build();
    }

    /**
     * 获取可用流转
     * GET /v1/agents/{id}/transitions
     */
    @GET
    @Path("/{id}/transitions")
    @RequiresPermission("agent:view")
    public Response getAvailableTransitions(@PathParam("id") UUID id) {
        AvailableTransitions transitions = stateMachineService.getAvailableTransitions(id);
        return Response.ok(transitions).build();
    }

    /**
     * 获取状态变更历史
     * GET /v1/agents/{id}/state-history
     */
    @GET
    @Path("/{id}/state-history")
    @RequiresPermission("agent:view")
    public Response getStateHistory(
            @PathParam("id") UUID id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        StateHistory history = stateMachineService.getStateHistory(id, page, size);
        return Response.ok(history).build();
    }

    /**
     * 批量状态流转
     * POST /v1/agents/batch-transition
     */
    @POST
    @Path("/batch-transition")
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

    /**
     * 强制状态流转（管理员操作，跳过校验）
     * POST /v1/agents/{id}/force-transition
     */
    @POST
    @Path("/{id}/force-transition")
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

    /**
     * 校验状态流转是否合法（不执行流转）
     * GET /v1/agents/validate-transition
     */
    @GET
    @Path("/validate-transition")
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

    /**
     * 获取所有状态流转规则
     * GET /v1/agents/state-rules
     */
    @GET
    @Path("/state-rules")
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