package com.easystation.agent.collaboration.spi;

import com.easystation.agent.collaboration.domain.AgentRole;
import com.easystation.agent.collaboration.dto.*;
import java.util.List;

/**
 * 协作服务接口
 */
public interface CollaborationService {

    // Session 管理
    CollaborationSessionDTO createSession(CreateSessionRequest request);
    CollaborationSessionDTO getSession(Long sessionId);
    List<CollaborationSessionDTO> listSessions();
    List<CollaborationSessionDTO> getActiveSessions();
    CollaborationSessionDTO closeSession(Long sessionId);
    CollaborationSessionDTO joinSession(Long sessionId, String agentId);
    CollaborationSessionDTO leaveSession(Long sessionId, String agentId);

    // Message 管理
    AgentMessageDTO sendMessage(SendMessageRequest request);
    List<AgentMessageDTO> getSessionMessages(Long sessionId);
    List<AgentMessageDTO> getMessagesForAgent(String agentId);
    AgentMessageDTO getMessageById(Long messageId);

    // Task 管理
    AgentTaskDTO createTask(Long sessionId, String title, String description, String priority);
    AgentTaskDTO getTask(Long taskId);
    List<AgentTaskDTO> getSessionTasks(Long sessionId);
    List<AgentTaskDTO> getTasksByStatus(String status);
    AgentTaskDTO assignTask(Long taskId, String agentId);
    AgentTaskDTO updateTaskStatus(Long taskId, String status);
    AgentTaskDTO completeTask(Long taskId, String result);
    AgentTaskDTO failTask(Long taskId, String error);
}
