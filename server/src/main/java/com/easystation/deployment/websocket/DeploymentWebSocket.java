package com.easystation.deployment.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 部署进度 WebSocket 端点
 * 路径: /ws/deployment/{deploymentId}
 * 支持前端订阅特定部署任务的进度
 */
@ServerEndpoint("/ws/deployment/{deploymentId}")
@ApplicationScoped
public class DeploymentWebSocket {

    @Inject
    ObjectMapper objectMapper;

    /**
     * 存储部署ID与WebSocket会话的映射
     * key: deploymentId, value: Set of Sessions
     */
    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("deploymentId") String deploymentId) {
        sessions.computeIfAbsent(deploymentId, k -> ConcurrentHashMap.newKeySet()).add(session);
        Log.infof("WebSocket connected to deployment %s, session %s", deploymentId, session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("deploymentId") String deploymentId) {
        Log.debugf("Received message from deployment %s: %s", deploymentId, message);
        // 目前客户端发送的消息不需要特殊处理，可以用于心跳或状态查询
    }

    @OnClose
    public void onClose(Session session, @PathParam("deploymentId") String deploymentId) {
        Set<Session> deploymentSessions = sessions.get(deploymentId);
        if (deploymentSessions != null) {
            deploymentSessions.remove(session);
            if (deploymentSessions.isEmpty()) {
                sessions.remove(deploymentId);
            }
        }
        Log.infof("WebSocket disconnected from deployment %s, session %s", deploymentId, session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("deploymentId") String deploymentId, Throwable throwable) {
        onClose(session, deploymentId);
        Log.errorf(throwable, "WebSocket error for deployment %s, session %s", deploymentId, session.getId());
    }

    /**
     * 向指定部署的所有订阅者广播消息
     */
    public void broadcast(UUID deploymentId, DeploymentProgressMessage message) {
        String deploymentIdStr = deploymentId.toString();
        Set<Session> deploymentSessions = sessions.get(deploymentIdStr);
        if (deploymentSessions != null && !deploymentSessions.isEmpty()) {
            try {
                String payload = objectMapper.writeValueAsString(message);
                deploymentSessions.forEach(s -> {
                    if (s != null && s.isOpen()) {
                        s.getAsyncRemote().sendText(payload);
                    }
                });
                Log.debugf("Broadcast to deployment %s: %d sessions", deploymentIdStr, deploymentSessions.size());
            } catch (Exception e) {
                Log.errorf(e, "Failed to broadcast message to deployment %s", deploymentIdStr);
            }
        }
    }

    /**
     * 推送进度更新
     */
    public void pushProgress(UUID deploymentId, int progress, String stage, String message) {
        DeploymentProgressMessage msg = new DeploymentProgressMessage();
        msg.type = "DEPLOYMENT_PROGRESS";
        msg.deploymentId = deploymentId.toString();
        msg.progress = progress;
        msg.stage = stage;
        msg.message = message;
        msg.timestamp = java.time.Instant.now().toString();
        broadcast(deploymentId, msg);
    }

    /**
     * 推送状态变更
     */
    public void pushStatus(UUID deploymentId, String status, String stage, String message) {
        DeploymentProgressMessage msg = new DeploymentProgressMessage();
        msg.type = "DEPLOYMENT_STATUS";
        msg.deploymentId = deploymentId.toString();
        msg.status = status;
        msg.stage = stage;
        msg.message = message;
        msg.timestamp = java.time.Instant.now().toString();
        broadcast(deploymentId, msg);
    }

    /**
     * 推送错误信息
     */
    public void pushError(UUID deploymentId, String error, String stage) {
        DeploymentProgressMessage msg = new DeploymentProgressMessage();
        msg.type = "DEPLOYMENT_ERROR";
        msg.deploymentId = deploymentId.toString();
        msg.status = "FAILED";
        msg.stage = stage;
        msg.message = error;
        msg.timestamp = java.time.Instant.now().toString();
        broadcast(deploymentId, msg);
    }

    /**
     * 部署进度消息格式
     */
    public static class DeploymentProgressMessage {
        public String type;
        public String deploymentId;
        public String status;
        public Integer progress;
        public String stage;
        public String message;
        public String timestamp;
    }
}