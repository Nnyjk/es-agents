package com.easystation.agent.collaboration.repository;

import com.easystation.agent.collaboration.domain.AgentMessage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AgentMessageRepository implements PanacheRepository<AgentMessage> {

    public List<AgentMessage> findBySessionId(Long sessionId) {
        return list("sessionId", sessionId);
    }

    public List<AgentMessage> findByFromAgentId(String fromAgentId) {
        return list("fromAgentId", fromAgentId);
    }

    public List<AgentMessage> findByToAgentId(String toAgentId) {
        return list("toAgentId", toAgentId);
    }

    public AgentMessage findByCorrelationId(Long correlationId) {
        return find("correlationId", correlationId).firstResult();
    }

    public List<AgentMessage> findMessagesForAgent(String agentId) {
        return list("toAgentId", agentId);
    }
}
