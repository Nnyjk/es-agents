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

    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("agentId") String agentId) {
        sessions.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet()).add(session);
        Log.infof("Console connected to agent %s", agentId);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("agentId") String agentId) {
        final UUID hostId;
        try {
            hostId = UUID.fromString(agentId);
        } catch (IllegalArgumentException e) {
            sendSystemMessage(session, "ERROR", "Invalid agentId");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText("");

            if ("FETCH_LOGS".equals(type)) {
                List<String> logs = agentLogService.readLogs(hostId, 100);
                String historyMsg = objectMapper.writeValueAsString(Map.of(
                        "type", "LOG_HISTORY",
                        "content", logs
                ));
                session.getAsyncRemote().sendText(historyMsg);
                return;
            }

            boolean sent = agentConnectionManager.get().send(hostId, message);
            if (!sent) {
                sendSystemMessage(session, "ERROR", "Agent connection unavailable");
            }
            return;
        } catch (Exception e) {
            Log.warnf("Failed to parse/route message, fallback raw forward: %s", e.getMessage());
        }

        try {
            boolean sent = agentConnectionManager.get().send(hostId, message);
            if (!sent) {
                sendSystemMessage(session, "ERROR", "Agent connection unavailable");
            }
        } catch (Exception e) {
            Log.errorf(e, "Failed to forward message to agent %s", agentId);
            sendSystemMessage(session, "ERROR", "Failed to forward command to agent");
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
            agentSessions.forEach(s -> {
                if (s.isOpen()) {
                    s.getAsyncRemote().sendText(logMessage);
                }
            });
        }
    }

    private void sendSystemMessage(Session session, String type, String content) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "content", content
            ));
            session.getAsyncRemote().sendText(payload);
        } catch (Exception e) {
            Log.warnf("Failed to send system message: %s", e.getMessage());
        }
    }
}
