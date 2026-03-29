package com.easystation.notification.listener;

import com.easystation.alert.domain.AlertEvent;
import com.easystation.deployment.domain.DeploymentProgress;
import com.easystation.notification.domain.NotificationMessage;
import com.easystation.notification.dto.NotificationRecord;
import com.easystation.notification.enums.MessageLevel;
import com.easystation.notification.enums.MessageType;
import com.easystation.notification.service.NotificationMessageService;
import com.easystation.notification.websocket.NotificationWebSocket;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * 消息通知事件监听器
 * 
 * 监听系统各类事件，自动创建站内消息并推送给用户
 */
@ApplicationScoped
public class NotificationEventListener {

    @Inject
    NotificationMessageService notificationService;

    @Inject
    NotificationWebSocket notificationWebSocket;

    /**
     * 监听告警事件
     */
    @Transactional
    public void onAlertEvent(@Observes AlertEvent alertEvent) {
        Log.infof("Received alert event: %s - %s", alertEvent.id, alertEvent.title);

        // 根据告警级别确定消息级别
        MessageLevel messageLevel = switch (alertEvent.level) {
            case CRITICAL, ERROR -> MessageLevel.ERROR;
            case WARNING -> MessageLevel.WARNING;
            default -> MessageLevel.INFO;
        };

        // 创建站内消息
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
            null,  // userId - TODO: 根据规则配置确定接收用户
            "system",  // username - 临时使用 system
            "告警通知：" + alertEvent.title,
            alertEvent.message,
            MessageType.ALERT,
            messageLevel,
            alertEvent.resourceType,
            alertEvent.resourceId,
            null  // jumpUrl
        );

        try {
            NotificationMessage message = notificationService.create(createRequest);
            Log.infof("Created notification message: %s", message.id);
            
            // 推送给在线用户
            // TODO: 根据用户订阅推送
        } catch (Exception e) {
            Log.errorf("Failed to create notification for alert: %s", e.getMessage());
        }
    }

    /**
     * 监听部署进展事件
     */
    @Transactional
    public void onDeploymentProgress(@Observes DeploymentProgress progress) {
        Log.infof("Received deployment progress event: %s - %s", progress.id, progress.status);

        // 根据部署状态确定消息级别和内容
        String title;
        String content;
        MessageLevel level;

        switch (progress.status) {
            case SUCCESS:
                title = "部署成功";
                content = String.format("部署任务 [%s] 已完成", progress.deploymentId);
                level = MessageLevel.INFO;
                break;
            case FAILED:
                title = "部署失败";
                content = String.format("部署任务 [%s] 失败", progress.deploymentId);
                level = MessageLevel.ERROR;
                break;
            default:
                return; // 其他状态不发送通知
        }

        NotificationRecord.Create createRequest = new NotificationRecord.Create(
            null,  // userId - TODO: 根据部署负责人确定
            "system",  // username
            title,
            content,
            MessageType.OPERATION,
            level,
            "DEPLOYMENT",
            progress.id,
            null  // jumpUrl
        );

        try {
            NotificationMessage message = notificationService.create(createRequest);
            Log.infof("Created notification message for deployment: %s", message.id);
        } catch (Exception e) {
            Log.errorf("Failed to create notification for deployment: %s", e.getMessage());
        }
    }

    /**
     * 定时推送未读消息给在线用户
     * 每 30 秒检查一次
     */
    @Scheduled(every = "30s")
    public void pushUnreadMessages() {
        int onlineUsers = NotificationWebSocket.getOnlineUserCount();
        if (onlineUsers == 0) {
            return;
        }

        Log.debugf("Pushing unread messages to %d online users", onlineUsers);
        
        // TODO: 获取所有在线用户的未读消息并推送
        // 需要扩展 NotificationWebSocket 支持广播或获取所有在线用户 ID
    }

    /**
     * 发送系统通知
     * 
     * @param title 通知标题
     * @param content 通知内容
     * @param level 通知级别
     * @param userId 接收用户 ID（null 表示所有用户）
     * @param username 用户名
     */
    public void sendSystemNotification(String title, String content, MessageLevel level, UUID userId, String username) {
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
            userId,
            username != null ? username : "system",
            title,
            content,
            MessageType.SYSTEM,
            level,
            null,
            null,
            null
        );

        try {
            NotificationMessage message = notificationService.create(createRequest);
            Log.infof("Created system notification: %s", message.id);
            
            // 如果指定了用户且在线，推送消息
            if (userId != null) {
                notificationWebSocket.pushNewMessage(
                    userId.toString(), 
                    title, 
                    content, 
                    MessageType.SYSTEM.name(), 
                    message.id.toString()
                );
            }
        } catch (Exception e) {
            Log.errorf("Failed to create system notification: %s", e.getMessage());
        }
    }
}
