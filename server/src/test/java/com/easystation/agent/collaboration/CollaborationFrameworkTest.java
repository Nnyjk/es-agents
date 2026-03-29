package com.easystation.agent.collaboration;

import com.easystation.agent.collaboration.domain.*;
import com.easystation.agent.collaboration.repository.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 协作框架集成测试
 */
@QuarkusTest
public class CollaborationFrameworkTest {

    @Inject
    CollaborationSessionRepository sessionRepository;

    @Inject
    AgentMessageRepository messageRepository;

    @Inject
    AgentTaskRepository taskRepository;

    @Test
    public void testCreateSession() {
        CollaborationSession session = new CollaborationSession();
        session.name = "Test Session";
        session.description = "Test Description";
        session.creatorAgentId = "agent-1";
        session.setAgentIds(Arrays.asList("agent-1", "agent-2"));
        session.status = "active";
        sessionRepository.persist(session);

        assertNotNull(session.id);
        assertEquals("Test Session", session.name);
        assertEquals("active", session.status);
    }

    @Test
    public void testCreateMessage() {
        CollaborationSession session = new CollaborationSession();
        session.name = "Message Test Session";
        session.creatorAgentId = "agent-1";
        session.setAgentIds(Arrays.asList("agent-1", "agent-2"));
        session.status = "active";
        sessionRepository.persist(session);

        AgentMessage message = new AgentMessage();
        message.sessionId = session.id;
        message.type = MessageType.REQUEST;
        message.fromAgentId = "agent-1";
        message.toAgentId = "agent-2";
        message.subject = "Test Message";
        message.content = "Hello, Agent 2!";
        message.status = "pending";
        messageRepository.persist(message);

        assertNotNull(message.id);
        assertEquals("pending", message.status);
        assertEquals(MessageType.REQUEST, message.type);
    }

    @Test
    public void testCreateTask() {
        CollaborationSession session = new CollaborationSession();
        session.name = "Task Test Session";
        session.creatorAgentId = "agent-1";
        session.setAgentIds(Arrays.asList("agent-1", "agent-2"));
        session.status = "active";
        sessionRepository.persist(session);

        AgentTask task = new AgentTask();
        task.sessionId = session.id;
        task.title = "Test Task";
        task.description = "Test Description";
        task.priority = "high";
        task.creatorAgentId = "agent-1";
        task.status = TaskStatus.PENDING;
        taskRepository.persist(task);

        assertNotNull(task.id);
        assertEquals(TaskStatus.PENDING, task.status);
        assertEquals("high", task.priority);
    }

    @Test
    public void testTaskLifecycle() {
        AgentTask task = new AgentTask();
        task.sessionId = 1L;
        task.title = "Lifecycle Test";
        task.creatorAgentId = "agent-1";
        task.status = TaskStatus.PENDING;
        taskRepository.persist(task);

        // Assign
        task.assignTo("agent-2");
        assertEquals(TaskStatus.ASSIGNED, task.status);
        assertNotNull(task.assignedAt);

        // Start
        task.start();
        assertEquals(TaskStatus.IN_PROGRESS, task.status);
        assertNotNull(task.startedAt);

        // Complete
        task.complete("{\"result\": \"success\"}");
        assertEquals(TaskStatus.COMPLETED, task.status);
        assertNotNull(task.completedAt);
        assertEquals("{\"result\": \"success\"}", task.result);
    }

    @Test
    public void testSessionAgentManagement() {
        CollaborationSession session = new CollaborationSession();
        session.name = "Agent Management Test";
        session.creatorAgentId = "agent-1";
        session.setAgentIds(Arrays.asList("agent-1"));
        session.status = "active";
        sessionRepository.persist(session);

        // Join
        List<String> agentIds = session.getAgentIdList();
        agentIds.add("agent-3");
        session.setAgentIds(agentIds);
        sessionRepository.persist(session);

        // Verify
        CollaborationSession updated = sessionRepository.findById(session.id);
        assertTrue(updated.getAgentIdList().contains("agent-3"));

        // Leave
        agentIds = updated.getAgentIdList();
        agentIds.remove("agent-3");
        updated.setAgentIds(agentIds);
        sessionRepository.persist(updated);

        // Verify
        CollaborationSession finalSession = sessionRepository.findById(session.id);
        assertFalse(finalSession.getAgentIdList().contains("agent-3"));
    }

    @Test
    public void testMessageCorrelation() {
        CollaborationSession session = new CollaborationSession();
        session.name = "Correlation Test";
        session.creatorAgentId = "agent-1";
        session.setAgentIds(Arrays.asList("agent-1", "agent-2"));
        session.status = "active";
        sessionRepository.persist(session);

        // Request message
        AgentMessage request = new AgentMessage();
        request.sessionId = session.id;
        request.type = MessageType.REQUEST;
        request.fromAgentId = "agent-1";
        request.toAgentId = "agent-2";
        request.subject = "Request";
        request.content = "Please do something";
        request.status = "read";
        messageRepository.persist(request);

        // Response message (correlated)
        AgentMessage response = new AgentMessage();
        response.sessionId = session.id;
        response.type = MessageType.RESPONSE;
        response.fromAgentId = "agent-2";
        response.toAgentId = "agent-1";
        response.correlationId = request.id;
        response.subject = "Response";
        response.content = "Done!";
        response.status = "read";
        messageRepository.persist(response);

        // Verify correlation
        AgentMessage fetchedResponse = messageRepository.findById(response.id);
        assertEquals(request.id, fetchedResponse.correlationId);
        assertEquals(MessageType.RESPONSE, fetchedResponse.type);
    }

    @Test
    public void testTaskOverdue() {
        AgentTask task = new AgentTask();
        task.sessionId = 1L;
        task.title = "Overdue Test";
        task.creatorAgentId = "agent-1";
        task.status = TaskStatus.IN_PROGRESS;
        task.deadline = LocalDateTime.now().minusHours(1); // 1 hour ago
        taskRepository.persist(task);

        assertTrue(task.isOverdue());
        assertFalse(task.isCompleted());
    }
}
