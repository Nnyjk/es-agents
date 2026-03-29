package com.easystation.notification.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 站内消息通知 WebSocket 端点
 * 路径：/ws/notification/{userId}
 * 支持前端订阅用户的实时消息通知
 * 
 * 功能：
 * - 用户连接时推送未读消息
 * - 新消息实时推送
 * - 消息状态变更通知（已读/删除）
 */
@ServerEndpoint("/ws/notification/{userId}")
@ApplicationScoped
public class NotificationWebSocket {

    @Inject
    ObjectMapper objectMapper;

    /**
     * 存储用户 ID 与 WebSocket 会话的映射
     * key: userId, value: Set of Sessions
     */
    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    /**
     * 连接建立时
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Log.infof("Notification WebSocket opened for user: %s, session: %s", userId, session.getId());
        
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        
        // 发送欢迎消息
        try {
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(Map.of(
                "type", "connected",
                "userId", userId,
                "message", "消息通知连接成功"
            )));
        } catch (Exception e) {
            Log.errorf("Failed to send welcome message: %s", e.getMessage());
        }
    }

    /**
     * 连接关闭时
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        Log.infof("Notification WebSocket closed for user: %s, session: %s", userId, session.getId());
        
        Set<Session> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
            }
        }
    }

    /**
     * 发生错误时
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("userId") String userId) {
        Log.errorf("Notification WebSocket error for user %s: %s", userId, throwable.getMessage());
    }

    /**
     * 收到消息时（前端可以发送确认消息）
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        Log.debugf("Received message from user %s: %s", userId, message);
        // 可以处理前端发来的确认消息
    }

    /**
     * 向特定用户推送消息
     * 
     * @param userId 用户 ID
     * @param data 消息数据（JSON 格式）
     */
    public void sendToUser(String userId, Object data) {
        Set<Session> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            Log.debugf("No active WebSocket sessions for user: %s", userId);
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            Log.errorf("Failed to serialize message: %s", e.getMessage());
            return;
        }

        for (Session session : userSessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(json);
                    Log.debugf("Pushed message to user %s, session %s", userId, session.getId());
                } catch (Exception e) {
                    Log.errorf("Failed to send message to session %s: %s", session.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 推送新消息通知
     * 
     * @param userId 接收用户 ID
     * @param messageTitle 消息标题
     * @param messageContent 消息内容
     * @param messageType 消息类型
     * @param messageId 消息 ID
     */
    public void pushNewMessage(String userId, String messageTitle, String messageContent, 
                               String messageType, String messageId) {
        sendToUser(userId, Map.of(
            "type", "new_message",
            "messageId", messageId,
            "title", messageTitle,
            "content", messageContent,
            "messageType", messageType,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 推送消息状态变更通知
     * 
     * @param userId 用户 ID
     * @param messageId 消息 ID
     * @param action 操作类型 (read/deleted)
     */
    public void pushMessageStatus(String userId, String messageId, String action) {
        sendToUser(userId, Map.of(
            "type", "message_status",
            "messageId", messageId,
            "action", action,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        return sessions.size();
    }

    /**
     * 获取特定用户的会话数量
     */
    public static int getUserSessionCount(String userId) {
        Set<Session> userSessions = sessions.get(userId);
        return userSessions != null ? userSessions.size() : 0;
    }
}
