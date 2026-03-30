package com.easystation.agent.collaboration.impl;

import com.easystation.agent.collaboration.domain.*;
import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.repository.*;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CollaborationServiceImpl implements CollaborationService {

    @Inject
    CollaborationSessionRepository sessionRepository;

    @Inject
    AgentMessageRepository messageRepository;

    @Inject
    AgentTaskRepository taskRepository;

    @Override
    @Transactional
    public CollaborationSessionDTO createSession(CreateSessionRequest request) {
        CollaborationSession session = new CollaborationSession();
        session.name = request.name;
        session.description = request.description;
        session.status = "active";
        session.agentIds = String.join(",", request.agentIds);
        session.creatorAgentId = request.creatorAgentId;
        sessionRepository.persist(session);
        return toDTO(session);
    }

    @Override
    public CollaborationSessionDTO getSession(Long sessionId) {
        CollaborationSession session = sessionRepository.findById(sessionId);
        return session != null ? toDTO(session) : null;
    }

    @Override
    public List<CollaborationSessionDTO> listSessions() {
        return sessionRepository.listAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CollaborationSessionDTO> getActiveSessions() {
        return sessionRepository.findActiveSessions().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CollaborationSessionDTO closeSession(Long sessionId) {
        CollaborationSession session = sessionRepository.findById(sessionId);
        if (session != null) {
            session.status = "closed";
            session.closedAt = java.time.LocalDateTime.now();
            sessionRepository.persist(session);
        }
        return session != null ? toDTO(session) : null;
    }

    @Override
    @Transactional
    public CollaborationSessionDTO joinSession(Long sessionId, String agentId) {
        CollaborationSession session = sessionRepository.findById(sessionId);
        if (session != null) {
            List<String> agentIds = Arrays.asList(session.agentIds != null ? session.agentIds.split(",") : new String[0]);
            if (!agentIds.contains(agentId)) {
                session.agentIds = session.agentIds != null ? session.agentIds + "," + agentId : agentId;
                sessionRepository.persist(session);
            }
        }
        return session != null ? toDTO(session) : null;
    }

    @Override
    @Transactional
    public CollaborationSessionDTO leaveSession(Long sessionId, String agentId) {
        CollaborationSession session = sessionRepository.findById(sessionId);
        if (session != null && session.agentIds != null) {
            List<String> agentIds = Arrays.asList(session.agentIds.split(","));
            agentIds.remove(agentId);
            session.agentIds = String.join(",", agentIds);
            sessionRepository.persist(session);
        }
        return session != null ? toDTO(session) : null;
    }

    @Override
    @Transactional
    public AgentMessageDTO sendMessage(SendMessageRequest request) {
        AgentMessage message = new AgentMessage();
        message.sessionId = request.sessionId;
        message.type = request.type;
        message.fromAgentId = request.fromAgentId;
        message.toAgentId = request.toAgentId;
        message.correlationId = request.correlationId;
        message.subject = request.subject;
        message.content = request.content;
        message.metadata = request.metadata;
        messageRepository.persist(message);
        return toDTO(message);
    }

    @Override
    public List<AgentMessageDTO> getSessionMessages(Long sessionId) {
        return messageRepository.findBySessionId(sessionId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AgentMessageDTO> getMessagesForAgent(String agentId) {
        return messageRepository.findMessagesForAgent(agentId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AgentMessageDTO getMessageById(Long messageId) {
        AgentMessage message = messageRepository.findById(messageId);
        return message != null ? toDTO(message) : null;
    }

    @Override
    @Transactional
    public AgentTaskDTO createTask(Long sessionId, String title, String description, String priority) {
        AgentTask task = new AgentTask();
        task.sessionId = sessionId;
        task.title = title;
        task.description = description;
        task.priority = priority != null ? priority : "medium";
        task.status = TaskStatus.PENDING;
        taskRepository.persist(task);
        return toDTO(task);
    }

    @Override
    public AgentTaskDTO getTask(Long taskId) {
        AgentTask task = taskRepository.findById(taskId);
        return task != null ? toDTO(task) : null;
    }

    @Override
    public List<AgentTaskDTO> getSessionTasks(Long sessionId) {
        return taskRepository.findBySessionId(sessionId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AgentTaskDTO> getTasksByStatus(String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        return taskRepository.findByStatus(taskStatus).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentTaskDTO assignTask(Long taskId, String agentId) {
        AgentTask task = taskRepository.findById(taskId);
        if (task != null) {
            task.assignedTo = agentId;
            task.status = TaskStatus.ASSIGNED;
            task.assignedAt = java.time.LocalDateTime.now();
            taskRepository.persist(task);
        }
        return task != null ? toDTO(task) : null;
    }

    @Override
    @Transactional
    public AgentTaskDTO updateTaskStatus(Long taskId, String status) {
        AgentTask task = taskRepository.findById(taskId);
        if (task != null) {
            task.status = TaskStatus.valueOf(status.toUpperCase());
            if (task.status == TaskStatus.IN_PROGRESS) {
                task.startedAt = java.time.LocalDateTime.now();
            }
            taskRepository.persist(task);
        }
        return task != null ? toDTO(task) : null;
    }

    @Override
    @Transactional
    public AgentTaskDTO completeTask(Long taskId, String result) {
        AgentTask task = taskRepository.findById(taskId);
        if (task != null) {
            task.status = TaskStatus.COMPLETED;
            task.result = result;
            task.completedAt = java.time.LocalDateTime.now();
            taskRepository.persist(task);
        }
        return task != null ? toDTO(task) : null;
    }

    @Override
    @Transactional
    public AgentTaskDTO failTask(Long taskId, String error) {
        AgentTask task = taskRepository.findById(taskId);
        if (task != null) {
            task.status = TaskStatus.FAILED;
            task.error = error;
            task.completedAt = java.time.LocalDateTime.now();
            taskRepository.persist(task);
        }
        return task != null ? toDTO(task) : null;
    }

    private CollaborationSessionDTO toDTO(CollaborationSession session) {
        CollaborationSessionDTO dto = new CollaborationSessionDTO();
        dto.id = session.id;
        dto.name = session.name;
        dto.description = session.description;
        dto.status = session.status;
        dto.agentIds = session.agentIds;
        dto.creatorAgentId = session.creatorAgentId;
        dto.createdAt = session.createdAt;
        dto.updatedAt = session.updatedAt;
        dto.closedAt = session.closedAt;
        return dto;
    }

    private AgentMessageDTO toDTO(AgentMessage message) {
        AgentMessageDTO dto = new AgentMessageDTO();
        dto.id = message.id;
        dto.sessionId = message.sessionId;
        dto.type = message.type;
        dto.fromAgentId = message.fromAgentId;
        dto.toAgentId = message.toAgentId;
        dto.correlationId = message.correlationId;
        dto.subject = message.subject;
        dto.content = message.content;
        dto.metadata = message.metadata;
        dto.createdAt = message.createdAt;
        return dto;
    }

    private AgentTaskDTO toDTO(AgentTask task) {
        AgentTaskDTO dto = new AgentTaskDTO();
        dto.id = task.id;
        dto.sessionId = task.sessionId;
        dto.title = task.title;
        dto.description = task.description;
        dto.taskType = task.taskType;
        dto.priority = task.priority;
        dto.status = task.status;
        dto.assignedTo = task.assignedTo;
        dto.createdBy = task.createdBy;
        dto.result = task.result;
        dto.error = task.error;
        dto.assignedAt = task.assignedAt;
        dto.startedAt = task.startedAt;
        dto.completedAt = task.completedAt;
        dto.createdAt = task.createdAt;
        dto.updatedAt = task.updatedAt;
        return dto;
    }
}
