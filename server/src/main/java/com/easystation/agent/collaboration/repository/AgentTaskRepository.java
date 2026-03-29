package com.easystation.agent.collaboration.repository;

import com.easystation.agent.collaboration.domain.AgentTask;
import com.easystation.agent.collaboration.domain.TaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AgentTaskRepository implements PanacheRepository<AgentTask> {

    public List<AgentTask> findBySessionId(Long sessionId) {
        return list("sessionId", sessionId);
    }

    public List<AgentTask> findByStatus(TaskStatus status) {
        return list("status", status);
    }

    public List<AgentTask> findByAssignedTo(String assignedTo) {
        return list("assignedTo", assignedTo);
    }

    public List<AgentTask> findBySessionIdAndStatus(Long sessionId, TaskStatus status) {
        return list("sessionId = ?1 and status = ?2", sessionId, status);
    }

    public List<AgentTask> findPendingTasks() {
        return list("status", TaskStatus.PENDING);
    }
}
