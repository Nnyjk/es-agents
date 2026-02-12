package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentTask;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.record.AgentInstanceRecord;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.agent.record.AgentInstanceRecord.Create;
import com.easystation.agent.record.AgentInstanceRecord.ExecuteCommand;
import com.easystation.agent.record.HeartbeatRequest;
import com.easystation.agent.record.AgentInstanceRecord.Update;
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
            if (host != null) instance.setHost(host);
        }
        
        if (dto.templateId() != null) {
            AgentTemplate template = AgentTemplate.findById(dto.templateId());
            if (template != null) instance.setTemplate(template);
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
            task.command.name,
            task.command.script,
            task.args,
            task.command.timeout
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
}
