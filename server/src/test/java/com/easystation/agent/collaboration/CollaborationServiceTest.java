package com.easystation.agent.collaboration;

import com.easystation.agent.collaboration.domain.*;
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
        CollaborationSession session = collaborationService.createSession(
                "Test Session",
                "Test Description",
                "agent-1",
                Arrays.asList("agent-1", "agent-2", "agent-3")
        );

        assertNotNull(session.id);
        assertEquals("Test Session", session.name);
        assertEquals("active", session.status);
        assertEquals("agent-1", session.creatorAgentId);
        assertTrue(session.getAgentIdList().contains("agent-2"));
    }

    @Test
    public void testJoinAndLeaveSession() {
        CollaborationSession session = collaborationService.createSession(
                "Join/Leave Test",
                null,
                "agent-1",
                Arrays.asList("agent-1")
        );

        // Join
        collaborationService.joinSession(session.id, "agent-4");
        CollaborationSession updated = collaborationService.getSession(session.id);
        assertTrue(updated.getAgentIdList().contains("agent-4"));

        // Leave
        collaborationService.leaveSession(session.id, "agent-4");
        CollaborationSession finalSession = collaborationService.getSession(session.id);
        assertFalse(finalSession.getAgentIdList().contains("agent-4"));
    }

    @Test
    public void testCloseSession() {
        CollaborationSession session = collaborationService.createSession(
                "Close Test",
                null,
                "agent-1",
                Arrays.asList("agent-1")
        );

        collaborationService.closeSession(session.id);
        CollaborationSession closed = collaborationService.getSession(session.id);
        assertEquals("closed", closed.status);
        assertNotNull(closed.closedAt);
    }

    @Test
    public void testSendMessage() {
        CollaborationSession session = collaborationService.createSession(
                "Message Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        AgentMessage message = collaborationService.sendMessage(
                session.id,
                "REQUEST",
                "agent-1",
                "agent-2",
                "Test Subject",
                "Test Content",
                null
        );

        assertNotNull(message.id);
        assertEquals(MessageType.REQUEST, message.type);
        assertEquals("pending", message.status);
    }

    @Test
    public void testGetUnreadMessages() {
        CollaborationSession session = collaborationService.createSession(
                "Unread Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        // Send messages
        collaborationService.sendMessage(session.id, "REQUEST", "agent-1", "agent-2", "Msg 1", "Content 1", null);
        collaborationService.sendMessage(session.id, "REQUEST", "agent-1", "agent-2", "Msg 2", "Content 2", null);

        List<AgentMessage> unread = collaborationService.getUnreadMessages(session.id, "agent-2");
        assertEquals(2, unread.size());

        // Mark as read
        collaborationService.markMessageAsRead(unread.get(0).id);
        unread = collaborationService.getUnreadMessages(session.id, "agent-2");
        assertEquals(1, unread.size());
    }

    @Test
    public void testCreateAndAssignTask() {
        CollaborationSession session = collaborationService.createSession(
                "Task Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        AgentTask task = collaborationService.createTask(
                session.id,
                "Test Task",
                "Test Description",
                "analysis",
                "high",
                "agent-1",
                "{\"param\": \"value\"}"
        );

        assertNotNull(task.id);
        assertEquals(TaskStatus.PENDING, task.status);
        assertEquals("high", task.priority);

        // Assign
        collaborationService.assignTask(task.id, "agent-2");
        AgentTask assigned = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.ASSIGNED, assigned.status);
        assertEquals("agent-2", assigned.assignedAgentId);
    }

    @Test
    public void testTaskLifecycle() {
        CollaborationSession session = collaborationService.createSession(
                "Lifecycle Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        AgentTask task = collaborationService.createTask(
                session.id,
                "Lifecycle Task",
                null,
                null,
                "medium",
                "agent-1",
                null
        );

        // Assign -> Start -> Complete
        collaborationService.assignTask(task.id, "agent-2");
        collaborationService.startTask(task.id);
        collaborationService.completeTask(task.id, "{\"result\": \"success\"}");

        AgentTask completed = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.COMPLETED, completed.status);
        assertNotNull(completed.completedAt);
        assertEquals("{\"result\": \"success\"}", completed.result);
    }

    @Test
    public void testFailTask() {
        CollaborationSession session = collaborationService.createSession(
                "Fail Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        AgentTask task = collaborationService.createTask(
                session.id,
                "Fail Task",
                null,
                null,
                "medium",
                "agent-1",
                null
        );

        collaborationService.assignTask(task.id, "agent-2");
        collaborationService.startTask(task.id);
        collaborationService.failTask(task.id, "Something went wrong");

        AgentTask failed = collaborationService.getTask(task.id);
        assertEquals(TaskStatus.FAILED, failed.status);
        assertEquals("Something went wrong", failed.errorMessage);
    }

    @Test
    public void testGetSessionTasks() {
        CollaborationSession session = collaborationService.createSession(
                "Session Tasks Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2")
        );

        collaborationService.createTask(session.id, "Task 1", null, null, "high", "agent-1", null);
        collaborationService.createTask(session.id, "Task 2", null, null, "medium", "agent-1", null);
        collaborationService.createTask(session.id, "Task 3", null, null, "low", "agent-1", null);

        List<AgentTask> allTasks = collaborationService.getSessionTasks(session.id, null);
        assertEquals(3, allTasks.size());

        List<AgentTask> pendingTasks = collaborationService.getSessionTasks(session.id, "pending");
        assertEquals(3, pendingTasks.size());
    }

    @Test
    public void testGetTasksByAgent() {
        CollaborationSession session = collaborationService.createSession(
                "Agent Tasks Test",
                null,
                "agent-1",
                Arrays.asList("agent-1", "agent-2", "agent-3")
        );

        AgentTask task1 = collaborationService.createTask(session.id, "Task 1", null, null, "high", "agent-1", null);
        AgentTask task2 = collaborationService.createTask(session.id, "Task 2", null, null, "medium", "agent-1", null);

        collaborationService.assignTask(task1.id, "agent-2");
        collaborationService.assignTask(task2.id, "agent-3");

        List<AgentTask> agent2Tasks = collaborationService.getTasksByAgent("agent-2", null);
        assertEquals(1, agent2Tasks.size());
        assertEquals("Task 1", agent2Tasks.get(0).title);

        List<AgentTask> agent3Tasks = collaborationService.getTasksByAgent("agent-3", null);
        assertEquals(1, agent3Tasks.size());
        assertEquals("Task 2", agent3Tasks.get(0).title);
    }
}
