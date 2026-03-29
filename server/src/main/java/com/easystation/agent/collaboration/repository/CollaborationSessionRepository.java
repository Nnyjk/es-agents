package com.easystation.agent.collaboration.repository;

import com.easystation.agent.collaboration.domain.CollaborationSession;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CollaborationSessionRepository implements PanacheRepository<CollaborationSession> {

    public CollaborationSession findByName(String name) {
        return find("name", name).firstResult();
    }

    public List<CollaborationSession> findByStatus(String status) {
        return list("status", status);
    }

    public List<CollaborationSession> findByCreatorAgentId(String creatorAgentId) {
        return list("creatorAgentId", creatorAgentId);
    }

    public List<CollaborationSession> findActiveSessions() {
        return list("status", "active");
    }
}
