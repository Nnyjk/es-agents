package com.easystation.agent.websocket;

import com.easystation.agent.service.AgentInstanceService;
import com.easystation.infra.domain.Host;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws/agent/{agentId}", configurator = AgentWebSocketConfigurator.class)
@ApplicationScoped
public class AgentWebSocket {

    @Inject
    AgentInstanceService agentInstanceService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsoleWebSocket consoleWebSocket;

    // Map<AgentID, Session>
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    @Transactional
    public void onOpen(Session session, @PathParam("agentId") String agentId) {
        String secret = (String) session.getUserProperties().get("X-Agent-Secret");
        
        if (secret == null || secret.isEmpty()) {
            Log.warnf("Agent %s connection rejected: No secret key provided", agentId);
            closeSession(session, "Missing Secret Key");
            return;
        }

        try {
            Host host = Host.findById(UUID.fromString(agentId));
            if (host == null) {
                Log.warnf("Agent %s connection rejected: Host not found", agentId);
                closeSession(session, "Host Not Found");
            } else if (!secret.equals(host.getSecretKey())) {
                Log.warnf("Agent %s connection rejected: Invalid secret key", agentId);
                closeSession(session, "Invalid Secret Key");
            } else {
                Log.infof("Agent %s connected and authenticated", agentId);
                sessions.put(agentId, session);
            }
        } catch (Exception e) {
            Log.errorf("Agent %s authentication error: %s", agentId, e.getMessage());
            closeSession(session, "Internal Error");
        }
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
        } catch (Exception e) {
            Log.debug("Failed to close session", e);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("agentId") String agentId) {
        sessions.remove(agentId);
        Log.infof("Agent %s disconnected", agentId);
    }

    @OnError
    public void onError(Session session, @PathParam("agentId") String agentId, Throwable throwable) {
        sessions.remove(agentId);
        Log.errorf(throwable, "Agent %s error", agentId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("agentId") String agentId) {
        try {
            if (message.contains("\"type\":\"LOG\"")) {
                // It's a log message, forward to Console
                consoleWebSocket.broadcastLog(agentId, message);
                return;
            }
        } catch (Exception e) {
            Log.errorf("Error handling message from agent %s: %s", agentId, e.getMessage());
        }
    }
}
