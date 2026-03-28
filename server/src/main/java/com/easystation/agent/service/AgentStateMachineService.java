package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentStatusSnapshot;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.agent.dto.AgentStateTransitionRecord;
import com.easystation.agent.dto.AgentStateTransitionRecord.AvailableTransitions;
import com.easystation.agent.dto.AgentStateTransitionRecord.StateChangeEntry;
import com.easystation.agent.dto.AgentStateTransitionRecord.StateHistory;
import com.easystation.agent.dto.AgentStateTransitionRecord.TransitionRequest;
import com.easystation.agent.dto.AgentStateTransitionRecord.TransitionResult;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.service.AlertEventService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 状态机服务
 * 管理 Agent 状态流转规则与校验
 */
@Slf4j
@ApplicationScoped
public class AgentStateMachineService {

    @Inject
    AlertEventService alertEventService;

    /**
     * 状态流转规则定义
     * Key: 当前状态, Value: 允许流转的目标状态列表
     */
    private static final Map<AgentStatus, Set<AgentStatus>> TRANSITION_RULES = new HashMap<>();

    /**
     * 状态超时配置（秒）
     */
    private static final Map<AgentStatus, Long> STATE_TIMEOUT_SECONDS = new HashMap<>();

    static {
        // 初始化状态流转规则
        // UNCONFIGURED → PREPARING → READY
        TRANSITION_RULES.put(AgentStatus.UNCONFIGURED, Set.of(AgentStatus.PREPARING, AgentStatus.ERROR));
        TRANSITION_RULES.put(AgentStatus.PREPARING, Set.of(AgentStatus.READY, AgentStatus.ERROR, AgentStatus.UNCONFIGURED));

        // READY → PACKAGING → PACKAGED → DEPLOYING → DEPLOYED → ONLINE
        TRANSITION_RULES.put(AgentStatus.READY, Set.of(AgentStatus.PACKAGING, AgentStatus.DEPLOYING, AgentStatus.ERROR));
        TRANSITION_RULES.put(AgentStatus.PACKAGING, Set.of(AgentStatus.PACKAGED, AgentStatus.ERROR, AgentStatus.READY));
        TRANSITION_RULES.put(AgentStatus.PACKAGED, Set.of(AgentStatus.DEPLOYING, AgentStatus.ERROR, AgentStatus.READY));
        TRANSITION_RULES.put(AgentStatus.DEPLOYING, Set.of(AgentStatus.DEPLOYED, AgentStatus.ERROR, AgentStatus.READY));
        TRANSITION_RULES.put(AgentStatus.DEPLOYED, Set.of(AgentStatus.ONLINE, AgentStatus.ERROR, AgentStatus.OFFLINE));

        // ONLINE ↔ OFFLINE
        TRANSITION_RULES.put(AgentStatus.ONLINE, Set.of(AgentStatus.OFFLINE, AgentStatus.ERROR));
        TRANSITION_RULES.put(AgentStatus.OFFLINE, Set.of(AgentStatus.ONLINE, AgentStatus.ERROR, AgentStatus.READY));

        // ERROR → READY (恢复)
        TRANSITION_RULES.put(AgentStatus.ERROR, Set.of(AgentStatus.READY, AgentStatus.UNCONFIGURED));

        // 初始化状态超时配置
        STATE_TIMEOUT_SECONDS.put(AgentStatus.PREPARING, 300L);   // 5分钟
        STATE_TIMEOUT_SECONDS.put(AgentStatus.PACKAGING, 600L);   // 10分钟
        STATE_TIMEOUT_SECONDS.put(AgentStatus.DEPLOYING, 300L);   // 5分钟
        STATE_TIMEOUT_SECONDS.put(AgentStatus.DEPLOYED, 60L);     // 1分钟（等待上线）
    }

    /**
     * 执行状态流转
     * @param instanceId Agent 实例 ID
     * @param request 流转请求
     * @return 流转结果
     */
    @Transactional
    public TransitionResult transition(UUID instanceId, TransitionRequest request) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        AgentStatus fromStatus = instance.status;
        AgentStatus toStatus = request.targetStatus();

        // 校验流转是否合法
        if (!isTransitionAllowed(fromStatus, toStatus)) {
            String message = String.format(
                "Invalid transition from %s to %s. Allowed: %s",
                fromStatus, toStatus, getAvailableTransitions(fromStatus)
            );
            log.warn("Agent [{}] state transition rejected: {}", instanceId, message);
            throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
        }

        // 执行状态变更
        instance.status = toStatus;
        instance.persist();

        // 记录状态快照
        createStatusSnapshot(instance, request.reason());

        // 触发告警事件
        triggerTransitionAlert(instance, fromStatus, toStatus, request.reason());

        log.info("Agent [{}] transitioned from {} to {}, reason: {}, operator: {}",
            instanceId, fromStatus, toStatus, request.reason(), request.operator());

        return new TransitionResult(
            instanceId,
            fromStatus,
            toStatus,
            true,
            String.format("Successfully transitioned from %s to %s", fromStatus, toStatus),
            LocalDateTime.now()
        );
    }

    /**
     * 获取可用流转列表
     * @param instanceId Agent 实例 ID
     * @return 可用流转信息
     */
    public AvailableTransitions getAvailableTransitions(UUID instanceId) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        AgentStatus currentStatus = instance.status;
        List<AgentStatus> availableTargets = getAvailableTransitions(currentStatus);

        String message = getStatusTransitionMessage(currentStatus);

        return new AvailableTransitions(instanceId, currentStatus, availableTargets, message);
    }

    /**
     * 获取状态变更历史
     * @param instanceId Agent 实例 ID
     * @param page 页码
     * @param size 每页大小
     * @return 状态变更历史
     */
    public StateHistory getStateHistory(UUID instanceId, int page, int size) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        List<AgentStatusSnapshot> snapshots = AgentStatusSnapshot.find(
            "agentInstance.id = ?1 order by snapshotTime desc",
            instanceId
        ).page(page, size).list();

        long total = AgentStatusSnapshot.count("agentInstance.id = ?1", instanceId);

        List<StateChangeEntry> history = snapshots.stream()
            .map(this::toStateChangeEntry)
            .collect(Collectors.toList());

        return new StateHistory(
            instanceId,
            instance.status,
            history,
            (int) total,
            page,
            size
        );
    }

    /**
     * 校验状态流转是否合法
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @return 是否允许流转
     */
    public boolean isTransitionAllowed(AgentStatus fromStatus, AgentStatus toStatus) {
        Set<AgentStatus> allowedTargets = TRANSITION_RULES.get(fromStatus);
        return allowedTargets != null && allowedTargets.contains(toStatus);
    }

    /**
     * 获取允许流转的目标状态列表
     * @param currentStatus 当前状态
     * @return 允许流转的目标状态列表
     */
    public List<AgentStatus> getAvailableTransitions(AgentStatus currentStatus) {
        Set<AgentStatus> allowed = TRANSITION_RULES.get(currentStatus);
        return allowed != null ? new ArrayList<>(allowed) : List.of();
    }

    /**
     * 创建状态快照
     */
    @Transactional
    public void createStatusSnapshot(AgentInstance instance, String extraInfo) {
        AgentStatusSnapshot snapshot = new AgentStatusSnapshot();
        snapshot.agentInstance = instance;
        snapshot.status = instance.status;
        snapshot.version = instance.version;
        snapshot.snapshotTime = LocalDateTime.now();

        // 计算心跳延迟
        if (instance.lastHeartbeatTime != null) {
            snapshot.heartbeatDelaySeconds = java.time.Duration.between(
                instance.lastHeartbeatTime, LocalDateTime.now()
            ).getSeconds();
        }

        snapshot.extraInfo = extraInfo;
        snapshot.persist();

        log.debug("Created status snapshot for Agent [{}]: status={}", instance.id, instance.status);
    }

    /**
     * 触发状态流转告警
     */
    private void triggerTransitionAlert(AgentInstance instance, AgentStatus fromStatus, AgentStatus toStatus, String reason) {
        String agentName = instance.template != null ? instance.template.name : "Unknown";
        String hostName = instance.host != null ? instance.host.name : "Unknown";

        AlertEventType eventType;
        AlertLevel level;

        // 根据状态变更类型确定告警事件和级别
        if (toStatus == AgentStatus.ERROR) {
            eventType = AlertEventType.AGENT_ERROR;
            level = AlertLevel.ERROR;
        } else if (toStatus == AgentStatus.OFFLINE && fromStatus == AgentStatus.ONLINE) {
            eventType = AlertEventType.AGENT_OFFLINE;
            level = AlertLevel.WARNING;
        } else if (toStatus == AgentStatus.ONLINE && fromStatus == AgentStatus.OFFLINE) {
            eventType = AlertEventType.AGENT_ONLINE;
            level = AlertLevel.INFO;
        } else if (toStatus == AgentStatus.DEPLOYED) {
            eventType = AlertEventType.AGENT_DEPLOYED;
            level = AlertLevel.INFO;
        } else {
            // 其他状态变更不触发告警
            return;
        }

        String title = String.format("Agent [%s] state changed: %s -> %s", agentName, fromStatus, toStatus);
        String message = String.format(
            "Agent [%s] on host [%s] state transitioned from %s to %s. Reason: %s",
            agentName, hostName, fromStatus, toStatus, reason != null ? reason : "N/A"
        );

        alertEventService.trigger(
            eventType,
            level,
            title,
            message,
            instance.id,
            "AgentInstance",
            null
        );
    }

    /**
     * 获取状态流转说明消息
     */
    private String getStatusTransitionMessage(AgentStatus status) {
        return switch (status) {
            case UNCONFIGURED -> "Agent needs configuration. Can transition to PREPARING.";
            case PREPARING -> "Agent is preparing. Can transition to READY after configuration.";
            case READY -> "Agent is ready. Can start packaging or deploy directly.";
            case PACKAGING -> "Agent is being packaged. Wait for completion.";
            case PACKAGED -> "Agent is packaged. Can proceed to deployment.";
            case DEPLOYING -> "Agent is deploying. Wait for completion.";
            case DEPLOYED -> "Agent is deployed. Should transition to ONLINE soon.";
            case ONLINE -> "Agent is online. Can go OFFLINE.";
            case OFFLINE -> "Agent is offline. Can recover to ONLINE or READY.";
            case ERROR -> "Agent encountered error. Can recover to READY or reset to UNCONFIGURED.";
        };
    }

    /**
     * 转换快照为状态变更记录
     */
    private StateChangeEntry toStateChangeEntry(AgentStatusSnapshot snapshot) {
        return new StateChangeEntry(
            snapshot.id,
            snapshot.status,
            snapshot.version,
            snapshot.heartbeatDelaySeconds,
            snapshot.snapshotTime,
            snapshot.extraInfo
        );
    }

    /**
     * 定时检查处于中间状态的 Agent 是否超时
     * 如 PREPARING、PACKAGING、DEPLOYING 状态超时则转为 ERROR
     */
    @Scheduled(every = "60s")
    @Transactional
    public void checkStateTimeout() {
        log.debug("开始检查 Agent 状态超时...");

        for (AgentStatus status : STATE_TIMEOUT_SECONDS.keySet()) {
            long timeoutSeconds = STATE_TIMEOUT_SECONDS.get(status);
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(timeoutSeconds);

            // 查询超时的 Agent（通过快照时间判断）
            List<AgentStatusSnapshot> lastSnapshots = AgentStatusSnapshot.find(
                "status = ?1 and snapshotTime < ?2",
                status, threshold
            ).list();

            for (AgentStatusSnapshot snapshot : lastSnapshots) {
                AgentInstance instance = snapshot.agentInstance;
                // 确认当前状态仍然一致（避免误判已流转的 Agent）
                if (instance.status == status) {
                    log.warn("Agent [{}] in status {} exceeded timeout of {} seconds, marking as ERROR",
                        instance.id, status, timeoutSeconds);

                    // 转为 ERROR 状态
                    instance.status = AgentStatus.ERROR;
                    instance.persist();

                    // 创建新的状态快照
                    createStatusSnapshot(instance, "State timeout: exceeded " + timeoutSeconds + "s in " + status);

                    // 触发告警
                    String agentName = instance.template != null ? instance.template.name : "Unknown";
                    String hostName = instance.host != null ? instance.host.name : "Unknown";
                    String message = String.format(
                        "Agent [%s] on host [%s] state %s exceeded timeout (%ds). Auto-transitioned to ERROR.",
                        agentName, hostName, status, timeoutSeconds
                    );

                    alertEventService.trigger(
                        AlertEventType.AGENT_ERROR,
                        AlertLevel.WARNING,
                        "Agent State Timeout: " + agentName,
                        message,
                        instance.id,
                        "AgentInstance",
                        null
                    );
                }
            }
        }

        log.debug("Agent 状态超时检查完成");
    }

    /**
     * 定时记录所有 Agent 状态快照
     * 每 5 分钟记录一次用于历史分析
     */
    @Scheduled(every = "5m")
    @Transactional
    public void recordPeriodicSnapshots() {
        log.debug("开始记录 Agent 状态快照...");

        List<AgentInstance> instances = AgentInstance.listAll();
        for (AgentInstance instance : instances) {
            createStatusSnapshot(instance, "Periodic snapshot");
        }

        log.debug("记录了 {} 个 Agent 状态快照", instances.size());
    }

    /**
     * 批量状态流转
     * @param instanceIds Agent 实例 ID 列表
     * @param request 流转请求
     * @return 流转结果列表
     */
    @Transactional
    public List<TransitionResult> batchTransition(List<UUID> instanceIds, TransitionRequest request) {
        List<TransitionResult> results = new ArrayList<>();

        for (UUID instanceId : instanceIds) {
            try {
                TransitionResult result = transition(instanceId, request);
                results.add(result);
            } catch (WebApplicationException e) {
                // 记录失败结果
                AgentInstance instance = AgentInstance.findById(instanceId);
                results.add(new TransitionResult(
                    instanceId,
                    instance != null ? instance.status : null,
                    request.targetStatus(),
                    false,
                    e.getMessage(),
                    LocalDateTime.now()
                ));
            }
        }

        return results;
    }

    /**
     * 强制重置 Agent 状态（跳过校验）
     * 仅用于管理员操作
     */
    @Transactional
    public TransitionResult forceTransition(UUID instanceId, AgentStatus targetStatus, String reason, String operator) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        AgentStatus fromStatus = instance.status;
        instance.status = targetStatus;
        instance.persist();

        createStatusSnapshot(instance, "Force transition: " + (reason != null ? reason : "N/A"));

        log.warn("Agent [{}] FORCE transitioned from {} to {} by {}, reason: {}",
            instanceId, fromStatus, targetStatus, operator, reason);

        triggerTransitionAlert(instance, fromStatus, targetStatus, "Force: " + reason);

        return new TransitionResult(
            instanceId,
            fromStatus,
            targetStatus,
            true,
            String.format("Force transitioned from %s to %s", fromStatus, targetStatus),
            LocalDateTime.now()
        );
    }
}