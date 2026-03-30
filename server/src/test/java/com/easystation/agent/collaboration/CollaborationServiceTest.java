package com.easystation.agent.collaboration;

import com.easystation.agent.collaboration.domain.*;
import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.impl.CollaborationServiceImpl;
import com.easystation.agent.collaboration.repository.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 协作服务测试
 */
@QuarkusTest
public class CollaborationServiceTest {

    @Inject
    CollaborationServiceImpl collaborationService;

    @Inject
    CollaborationSessionRepository sessionRepository;

    @Inject
    AgentMessageRepository messageRepository;

    @Inject
    AgentTaskRepository taskRepository;

    @Test
    public void testCreateSession() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Test Session";
        req.description = "Test Description";
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2", "agent-3"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        assertNotNull(session.id);
        assertEquals("Test Session", session.name);
        assertEquals("active", session.status);
        assertEquals("agent-1", session.creatorAgentId);
        assertTrue(Arrays.asList(session.agentIds.split(",")).contains("agent-2"));
    }

    @Test
    public void testJoinAndLeaveSession() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Join/Leave Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        // Join
        collaborationService.joinSession(session.id, "agent-4");
        CollaborationSessionDTO updated = collaborationService.getSession(session.id);
        assertTrue(Arrays.asList(updated.agentIds.split(",")).contains("agent-4"));

        // Leave
        collaborationService.leaveSession(session.id, "agent-4");
        CollaborationSessionDTO finalSession = collaborationService.getSession(session.id);
        assertFalse(Arrays.asList(finalSession.agentIds.split(",")).contains("agent-4"));
    }

    @Test
    public void testCloseSession() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Close Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        collaborationService.closeSession(session.id);
        CollaborationSessionDTO closed = collaborationService.getSession(session.id);
        assertEquals("closed", closed.status);
        assertNotNull(closed.closedAt);
    }

    @Test
    public void testSendMessage() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Message Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.sessionId = session.id;
        msgReq.type = MessageType.TASK_REQUEST;
        msgReq.fromAgentId = "agent-1";
        msgReq.toAgentId = "agent-2";
        msgReq.subject = "Test Subject";
        msgReq.content = "Test Content";
        msgReq.correlationId = null;
        AgentMessageDTO message = collaborationService.sendMessage(msgReq);

        assertNotNull(message.id);
        assertEquals(MessageType.TASK_REQUEST, message.type);
    }

    @Test
    public void testCreateAndAssignTask() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Task Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        AgentTaskDTO task = collaborationService.createTask(
                session.id,
                "Test Task",
                "Test Description",
                "high"
        );

        assertNotNull(task.id);
        assertEquals(TaskStatus.PENDING, task.status);
        assertEquals("high", task.priority);

        // Assign
        collaborationService.assignTask(task.id, "agent-2");
        AgentTaskDTO assigned = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.ASSIGNED, assigned.status);
        assertEquals("agent-2", assigned.assignedTo);
    }

    @Test
    public void testTaskLifecycle() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Lifecycle Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        AgentTaskDTO task = collaborationService.createTask(
                session.id,
                "Lifecycle Task",
                null,
                "medium"
        );

        // Assign -> Start -> Complete
        collaborationService.assignTask(task.id, "agent-2");
        collaborationService.updateTaskStatus(task.id, "IN_PROGRESS");
        collaborationService.completeTask(task.id, "{\"result\": \"success\"}");

        AgentTaskDTO completed = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.COMPLETED, completed.status);
        assertNotNull(completed.completedAt);
        assertEquals("{\"result\": \"success\"}", completed.result);
    }

    @Test
    public void testFailTask() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Fail Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        AgentTaskDTO task = collaborationService.createTask(
                session.id,
                "Fail Task",
                null,
                "medium"
        );

        collaborationService.assignTask(task.id, "agent-2");
        collaborationService.updateTaskStatus(task.id, "IN_PROGRESS");
        collaborationService.failTask(task.id, "Something went wrong");

        AgentTaskDTO failed = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.FAILED, failed.status);
        assertEquals("Something went wrong", failed.error);
    }

    @Test
    public void testGetSessionTasks() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.name = "Session Tasks Test";
        req.description = null;
        req.creatorAgentId = "agent-1";
        req.agentIds = new String[]{"agent-1", "agent-2"};
        CollaborationSessionDTO session = collaborationService.createSession(req);

        collaborationService.createTask(session.id, "Task 1", null, "high");
        collaborationService.createTask(session.id, "Task 2", null, "medium");
        collaborationService.createTask(session.id, "Task 3", null, "low");

        List<AgentTaskDTO> allTasks = collaborationService.getSessionTasks(session.id);
        assertEquals(3, allTasks.size());

        List<AgentTaskDTO> pendingTasks = collaborationService.getTasksByStatus("PENDING");
        assertTrue(pendingTasks.size() >= 3);
    }
}