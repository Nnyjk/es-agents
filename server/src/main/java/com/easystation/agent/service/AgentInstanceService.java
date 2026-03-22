package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentTask;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.dto.AgentHealthRecord;
import com.easystation.agent.dto.AgentHealthRecord.HealthStatus;
import com.easystation.agent.dto.AgentInstanceRecord;
import com.easystation.agent.dto.AgentInstanceRecord.Create;
import com.easystation.agent.dto.AgentInstanceRecord.Deploy;
import com.easystation.agent.dto.AgentInstanceRecord.DeployResult;
import com.easystation.agent.dto.AgentInstanceRecord.ExecuteCommand;
import com.easystation.agent.dto.AgentInstanceRecord.Update;
import com.easystation.agent.dto.AgentRuntimeStatus;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.agent.dto.HeartbeatRequest;
import com.easystation.infra.domain.Host;
import com.easystation.infra.domain.enums.HostStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentInstanceService {

    public List<AgentInstanceRecord> list(UUID hostId) {
        String query = "FROM AgentInstance i LEFT JOIN FETCH i.host LEFT JOIN FETCH i.template";
        List<AgentInstance> list = hostId != null
            ? AgentInstance.list(query + " WHERE i.host.id = ?1", hostId)
            : AgentInstance.list(query);

        return list.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public AgentInstanceRecord get(UUID id) {
        AgentInstance instance = AgentInstance.findById(id);
        if (instance == null) {
            throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);
        }
        return toDto(instance);
    }

    @Transactional
    public AgentInstanceRecord create(Create dto) {
        Host host = Host.findById(dto.hostId());
        AgentTemplate template = AgentTemplate.findById(dto.templateId());

        if (host == null) throw new WebApplicationException("Host not found", Response.Status.BAD_REQUEST);
        if (template == null) throw new WebApplicationException("Template not found", Response.Status.BAD_REQUEST);

        // Validate host reachability for binding
        validateHostReachability(host);

        AgentInstance instance = new AgentInstance();
        instance.setHost(host);
        instance.setTemplate(template);
        instance.setStatus(AgentStatus.UNCONFIGURED);
        
        instance.persist();
        return toDto(instance);
    }

    @Transactional
    public AgentInstanceRecord update(UUID id, Update dto) {
        AgentInstance instance = AgentInstance.findById(id);
        if (instance == null) {
            throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);
        }
        
        if (dto.hostId() != null) {
            Host host = Host.findById(dto.hostId());
            if (host == null) {
                throw new WebApplicationException("Host not found", Response.Status.BAD_REQUEST);
            }
            // Validate host reachability for binding
            validateHostReachability(host);
            instance.setHost(host);
        }
        
        if (dto.templateId() != null) {
            AgentTemplate template = AgentTemplate.findById(dto.templateId());
            if (template == null) {
                throw new WebApplicationException("Template not found", Response.Status.BAD_REQUEST);
            }
            instance.setTemplate(template);
        }
        
        return toDto(instance);
    }

    @Transactional
    public void delete(UUID id) {
        if (!AgentInstance.deleteById(id)) {
            throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public void executeCommand(UUID instanceId, ExecuteCommand dto) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);

        AgentCommand command = AgentCommand.findById(dto.commandId());
        if (command == null) throw new WebApplicationException("Command not found", Response.Status.NOT_FOUND);

        AgentTask task = new AgentTask();
        task.agentInstance = instance;
        task.command = command;
        task.args = dto.args();
        task.status = AgentTaskStatus.PENDING;
        task.persist();
    }

    @Transactional
    public List<AgentTaskRecord> fetchPendingTasks(String agentIdStr, String secretKey) {
        UUID agentId;
        try {
            agentId = UUID.fromString(agentIdStr);
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        // Check Host first
        Host host = Host.findById(agentId);
        if (host != null) {
            if (secretKey == null || !secretKey.equals(host.getSecretKey())) {
                throw new WebApplicationException("Invalid secret key", Response.Status.UNAUTHORIZED);
            }
        } else {
            // Check AgentInstance
            AgentInstance instance = AgentInstance.findById(agentId);
            if (instance != null) {
                Host h = Host.findById(instance.getHost().getId());
                if (h == null || secretKey == null || !secretKey.equals(h.getSecretKey())) {
                    throw new WebApplicationException("Invalid secret key", Response.Status.UNAUTHORIZED);
                }
            } else {
                throw new WebApplicationException("Agent/Host not found", Response.Status.NOT_FOUND);
            }
        }

        List<AgentTask> tasks = AgentTask.list("agentInstance.id = ?1 and status = ?2", agentId, AgentTaskStatus.PENDING);
        if (tasks.isEmpty()) {
            return List.of();
        }

        tasks.forEach(t -> t.status = AgentTaskStatus.SENT);
        AgentTask.persist(tasks);

        return tasks.stream().map(task -> new AgentTaskRecord(
            task.id,
            task.agentInstance.getId(),
            task.agentInstance.getTemplate().getName(),
            task.command.name,
            task.args,
            task.result,
            task.status,
            null, // durationMs - not available
            task.createdAt,
            task.updatedAt
        )).toList();
    }

    @Transactional
    public void handleHeartbeat(HeartbeatRequest request, String secretKey) {
        AgentInstance instance = AgentInstance.findById(request.agentId());
        Host host = Host.findById(request.agentId());

        if (instance == null && host == null) {
            throw new WebApplicationException("Agent/Host not found", Response.Status.NOT_FOUND);
        }

        Host hostToCheck = null;
        if (host != null) {
            hostToCheck = host;
        } else {
            hostToCheck = Host.findById(instance.getHost().getId());
        }

        if (hostToCheck == null || secretKey == null || !secretKey.equals(hostToCheck.getSecretKey())) {
             throw new WebApplicationException("Invalid secret key", Response.Status.UNAUTHORIZED);
        }

        if (instance != null) {
            instance.status = AgentStatus.ONLINE;
            instance.lastHeartbeatTime = LocalDateTime.now();
            if (request.version() != null) {
                instance.version = request.version();
            }
            instance.persist();
        }

        if (host != null) {
            host.setLastHeartbeat(LocalDateTime.now());
            host.setStatus(HostStatus.ONLINE);
            host.persist();
        }
    }

    /**
     * 部署 Agent 实例到目标主机
     */
    @Transactional
    public DeployResult deploy(UUID instanceId, Deploy request) {
        AgentInstance instance = AgentInstance.findById(instanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        // 检查前置条件：Agent 必须处于 READY 或 PACKAGED 状态
        if (instance.status != AgentStatus.READY && instance.status != AgentStatus.PACKAGED) {
            throw new WebApplicationException(
                "Agent must be in READY or PACKAGED status to deploy, current: " + instance.status,
                Response.Status.BAD_REQUEST
            );
        }

        // 更新状态为部署中
        instance.status = AgentStatus.DEPLOYING;
        instance.version = request.version();
        instance.persist();

        // TODO: 实际的部署逻辑
        // 1. 通过 WebSocket/SSH 连接到目标主机
        // 2. 传输安装包
        // 3. 执行安装脚本
        // 4. 验证部署结果
        // 5. 更新状态为 DEPLOYED 或 ERROR

        // 模拟部署成功
        instance.status = AgentStatus.DEPLOYED;
        instance.persist();

        return new DeployResult(
            instance.id,
            instance.status,
            "Deployment completed successfully",
            LocalDateTime.now()
        );
    }

    public List<AgentTaskRecord> queryTaskHistory(UUID agentInstanceId, AgentTaskStatus status, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if (agentInstanceId != null) {
            query.append(" and agentInstance.id = ?").append(params.size() + 1);
            params.add(agentInstanceId);
        }

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }

        if (startTime != null) {
            query.append(" and createdAt >= ?").append(params.size() + 1);
            params.add(startTime);
        }

        if (endTime != null) {
            query.append(" and createdAt <= ?").append(params.size() + 1);
            params.add(endTime);
        }

        query.append(" order by createdAt desc");

        List<AgentTask> tasks = AgentTask.find(query.toString(), params.toArray()).page(page, size).list();

        return tasks.stream().map(this::toTaskRecord).toList();
    }

    public AgentTaskRecord getTaskDetail(UUID taskId) {
        AgentTask task = AgentTask.findById(taskId);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        return toTaskRecord(task);
    }

    public long countTaskHistory(UUID agentInstanceId, AgentTaskStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if (agentInstanceId != null) {
            query.append(" and agentInstance.id = ?").append(params.size() + 1);
            params.add(agentInstanceId);
        }

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }

        if (startTime != null) {
            query.append(" and createdAt >= ?").append(params.size() + 1);
            params.add(startTime);
        }

        if (endTime != null) {
            query.append(" and createdAt <= ?").append(params.size() + 1);
            params.add(endTime);
        }

        return AgentTask.count(query.toString(), params.toArray());
    }

    private AgentTaskRecord toTaskRecord(AgentTask task) {
        Long durationMs = null;
        if (task.updatedAt != null && task.createdAt != null) {
            durationMs = java.time.Duration.between(task.createdAt, task.updatedAt).toMillis();
        }

        return new AgentTaskRecord(
            task.id,
            task.agentInstance.id,
            task.agentInstance.host.name,
            task.command.name,
            task.args,
            task.result,
            task.status,
            durationMs,
            task.createdAt,
            task.updatedAt
        );
    }

    private AgentInstanceRecord toDto(AgentInstance instance) {
        return new AgentInstanceRecord(
            instance.id,
            instance.host.id,
            instance.host.name,
            instance.template.id,
            instance.template.name,
            instance.status,
            instance.version,
            instance.createdAt,
            instance.updatedAt
        );
    }

    /**
     * Validate host reachability for instance binding.
     * Host should be ONLINE or at least have a valid gatewayUrl configured.
     */
    private void validateHostReachability(Host host) {
        // Check if host has gatewayUrl configured
        if (host.getGatewayUrl() == null || host.getGatewayUrl().isBlank()) {
            throw new WebApplicationException(
                "Host does not have gatewayUrl configured. Cannot bind instance to unreachable host.",
                Response.Status.BAD_REQUEST
            );
        }
        // Note: We allow binding to OFFLINE hosts as they might come online later.
        // The actual connection will be attempted when deployment is triggered.
    }

    /**
     * 获取 Agent 实时运行状态
     */
    public AgentRuntimeStatus getRuntimeStatus(UUID id) {
        AgentInstance instance = AgentInstance.findById(id);
        if (instance == null) {
            throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);
        }

        Long heartbeatAgeSeconds = null;
        if (instance.lastHeartbeatTime != null) {
            heartbeatAgeSeconds = java.time.Duration.between(
                instance.lastHeartbeatTime, 
                LocalDateTime.now()
            ).getSeconds();
        }

        boolean isOnline = instance.status == AgentStatus.ONLINE || instance.status == AgentStatus.DEPLOYED;
        String statusMessage = getStatusMessage(instance.status);

        return new AgentRuntimeStatus(
            instance.id,
            instance.status,
            instance.version,
            instance.lastHeartbeatTime,
            heartbeatAgeSeconds,
            isOnline,
            statusMessage,
            instance.createdAt,
            instance.updatedAt
        );
    }

    /**
     * 获取 Agent 健康度信息
     */
    public AgentHealthRecord getHealth(UUID id) {
        AgentInstance instance = AgentInstance.findById(id);
        if (instance == null) {
            throw new WebApplicationException("Agent Instance not found", Response.Status.NOT_FOUND);
        }

        AgentHealthRecord.Builder builder = AgentHealthRecord.builder()
            .id(instance.id)
            .agentName(instance.template != null ? instance.template.name : "Unknown");

        // 计算心跳年龄
        Long heartbeatAgeSeconds = null;
        if (instance.lastHeartbeatTime != null) {
            heartbeatAgeSeconds = java.time.Duration.between(
                instance.lastHeartbeatTime, 
                LocalDateTime.now()
            ).getSeconds();
            builder.lastHeartbeatTime(instance.lastHeartbeatTime)
                   .heartbeatAgeSeconds(heartbeatAgeSeconds);
        }

        // 查询最近24小时的任务统计
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long totalTasks = AgentTask.count("agentInstance.id = ?1 and createdAt >= ?2", id, since);
        long successfulTasks = AgentTask.count(
            "agentInstance.id = ?1 and createdAt >= ?2 and status = ?3", 
            id, since, AgentTaskStatus.SUCCESS
        );
        long failedTasks = AgentTask.count(
            "agentInstance.id = ?1 and createdAt >= ?2 and status = ?3", 
            id, since, AgentTaskStatus.FAILED
        );

        builder.totalTasks24h(totalTasks)
               .successfulTasks24h(successfulTasks)
               .failedTasks24h(failedTasks);

        // 查询最近成功和失败的任务时间
        AgentTask lastSuccess = AgentTask.find(
            "agentInstance.id = ?1 and status = ?2 order by createdAt desc", 
            id, AgentTaskStatus.SUCCESS
        ).firstResult();
        if (lastSuccess != null) {
            builder.lastSuccessfulTaskTime(lastSuccess.createdAt);
        }

        AgentTask lastFailed = AgentTask.find(
            "agentInstance.id = ?1 and status = ?2 order by createdAt desc", 
            id, AgentTaskStatus.FAILED
        ).firstResult();
        if (lastFailed != null) {
            builder.lastFailedTaskTime(lastFailed.createdAt);
        }

        // 计算健康状态
        HealthStatus healthStatus = calculateHealthStatus(
            instance.status, 
            heartbeatAgeSeconds, 
            totalTasks, 
            failedTasks,
            builder
        );
        builder.status(healthStatus);

        return builder.build();
    }

    /**
     * 计算健康状态
     */
    private HealthStatus calculateHealthStatus(
        AgentStatus status, 
        Long heartbeatAgeSeconds, 
        long totalTasks, 
        long failedTasks,
        AgentHealthRecord.Builder builder
    ) {
        List<String> issues = new ArrayList<>();

        // 检查 Agent 状态
        if (status == AgentStatus.ERROR) {
            builder.addIssue("Agent is in ERROR state");
            return HealthStatus.CRITICAL;
        }

        if (status == AgentStatus.OFFLINE || status == AgentStatus.UNCONFIGURED) {
            builder.addIssue("Agent is " + status.name());
            return HealthStatus.WARNING;
        }

        // 检查心跳
        if (heartbeatAgeSeconds == null) {
            builder.addIssue("No heartbeat received");
            return HealthStatus.UNKNOWN;
        }

        if (heartbeatAgeSeconds > 300) { // 5分钟无心跳
            builder.addIssue("Heartbeat timeout: " + heartbeatAgeSeconds + " seconds ago");
            return HealthStatus.CRITICAL;
        }

        if (heartbeatAgeSeconds > 60) { // 1分钟无心跳
            builder.addIssue("Heartbeat delayed: " + heartbeatAgeSeconds + " seconds ago");
            return HealthStatus.WARNING;
        }

        // 检查任务失败率
        if (totalTasks > 0) {
            double failureRate = (double) failedTasks / totalTasks * 100;
            if (failureRate > 50) {
                builder.addIssue("High task failure rate: " + String.format("%.1f%%", failureRate));
                return HealthStatus.CRITICAL;
            }
            if (failureRate > 20) {
                builder.addIssue("Elevated task failure rate: " + String.format("%.1f%%", failureRate));
                return HealthStatus.WARNING;
            }
        }

        return HealthStatus.HEALTHY;
    }

    /**
     * 获取状态描述信息
     */
    private String getStatusMessage(AgentStatus status) {
        return switch (status) {
            case UNCONFIGURED -> "Agent not configured";
            case PREPARING -> "Agent preparing";
            case READY -> "Agent ready for deployment";
            case PACKAGING -> "Agent packaging";
            case PACKAGED -> "Agent packaged";
            case DEPLOYING -> "Agent deploying";
            case DEPLOYED -> "Agent deployed";
            case ONLINE -> "Agent online";
            case OFFLINE -> "Agent offline";
            case ERROR -> "Agent error";
        };
    }
}