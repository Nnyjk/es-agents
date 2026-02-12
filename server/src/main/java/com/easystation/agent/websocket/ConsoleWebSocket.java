package com.easystation.agent.websocket;

import com.easystation.agent.service.AgentLogService;
import com.easystation.infra.socket.AgentConnectionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/console/{agentId}")
@ApplicationScoped
public class ConsoleWebSocket {

    @Inject
    Instance<AgentConnectionManager> agentConnectionManager;

    @Inject
    AgentLogService agentLogService;

    @Inject
    ObjectMapper objectMapper;

    // Map<AgentID, Set<Session>> - One agent can be watched by multiple consoles
    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("agentId") String agentId) {
        sessions.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet()).add(session);
        Log.infof("Console connected to agent %s", agentId);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("agentId") String agentId) {
        // Intercept FETCH_LOGS
        try {
            if (message.contains("FETCH_LOGS")) {
                JsonNode root = objectMapper.readTree(message);
                if ("FETCH_LOGS".equals(root.path("type").asText())) {
                    List<String> logs = agentLogService.readLogs(UUID.fromString(agentId), 100);
                    // Construct LOG_HISTORY message
                    String historyMsg = objectMapper.writeValueAsString(Map.of(
                        "type", "LOG_HISTORY",
                        "content", logs
                    ));
                    session.getAsyncRemote().sendText(historyMsg);
                    return; // Handled locally, don't forward
                }
            }
        } catch (Exception e) {
            Log.warnf("Failed to process message locally: %s", e.getMessage());
        }

        // Forward to Agent via AgentConnectionManager
        try {
            agentConnectionManager.get().send(UUID.fromString(agentId), message);
        } catch (Exception e) {
            Log.errorf(e, "Failed to forward message to agent %s", agentId);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("agentId") String agentId) {
        Set<Session> agentSessions = sessions.get(agentId);
        if (agentSessions != null) {
            agentSessions.remove(session);
            if (agentSessions.isEmpty()) {
                sessions.remove(agentId);
            }
        }
        Log.infof("Console disconnected from agent %s", agentId);
    }

    @OnError
    public void onError(Session session, @PathParam("agentId") String agentId, Throwable throwable) {
        onClose(session, agentId);
        Log.errorf(throwable, "Console error for agent %s", agentId);
    }

    public void broadcastLog(String agentId, String logMessage) {
        Set<Session> agentSessions = sessions.get(agentId);
        if (agentSessions != null) {
            agentSessions.forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(logMessage);
                }
            });
        }
    }
}
