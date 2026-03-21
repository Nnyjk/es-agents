package com.easystation.alert.service;

import com.easystation.alert.domain.AlertChannel;
import com.easystation.alert.domain.AlertEvent;
import com.easystation.alert.domain.AlertRule;
import com.easystation.alert.enums.AlertChannelType;
import com.easystation.alert.enums.AlertStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 告警通知服务
 */
@ApplicationScoped
public class AlertNotificationService {

    @Inject
    ObjectMapper objectMapper;

    private final Client client = ClientBuilder.newClient();

    /**
     * 发送告警通知
     */
    @Transactional
    public void notify(AlertEvent event) {
        if (event.ruleId == null) {
            Log.infof("Alert event %s has no rule, skipping notification", event.id);
            return;
        }

        AlertRule rule = AlertRule.findById(event.ruleId);
        if (rule == null || rule.channelIds == null || rule.channelIds.isEmpty()) {
            Log.infof("Alert rule %s has no channels, skipping notification", event.ruleId);
            return;
        }

        for (UUID channelId : rule.channelIds) {
            AlertChannel channel = AlertChannel.findById(channelId);
            if (channel == null || !channel.enabled) {
                continue;
            }

            try {
                sendNotification(channel, event);
                event.status = AlertStatus.NOTIFIED;
                event.lastNotifiedAt = LocalDateTime.now();
                event.count++;
            } catch (Exception e) {
                Log.errorf(e, "Failed to send notification via channel %s", channel.id);
            }
        }
    }

    private void sendNotification(AlertChannel channel, AlertEvent event) {
        switch (channel.type) {
            case EMAIL -> sendEmail(channel, event);
            case WECHAT_WORK -> sendWechatWork(channel, event);
            case DINGTALK -> sendDingTalk(channel, event);
            case WEBHOOK -> sendWebhook(channel, event);
            case SMS -> sendSms(channel, event);
            default -> Log.warnf("Unknown channel type: %s", channel.type);
        }
    }

    private void sendEmail(AlertChannel channel, AlertEvent event) {
        // TODO: 实现邮件发送
        Log.infof("Sending email notification for event %s via channel %s", event.id, channel.id);
        // 实际实现需要注入邮件服务
    }

    private void sendWechatWork(AlertChannel channel, AlertEvent event) {
        Map<String, Object> config = parseConfig(channel.config);
        String webhookUrl = (String) config.get("webhookUrl");

        if (webhookUrl == null || webhookUrl.isBlank()) {
            Log.warnf("WeChat Work channel %s has no webhook URL", channel.id);
            return;
        }

        Map<String, Object> message = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "content", String.format("## %s\n\n**级别**: %s\n\n**类型**: %s\n\n**内容**: %s",
                                event.title, event.level, event.eventType, event.message)
                )
        );

        try {
            Response response = client.target(webhookUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(message));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("WeChat Work notification failed: " + response.getStatus());
            }
            Log.infof("Sent WeChat Work notification for event %s", event.id);
        } catch (Exception e) {
            Log.errorf(e, "Failed to send WeChat Work notification");
            throw new RuntimeException(e);
        }
    }

    private void sendDingTalk(AlertChannel channel, AlertEvent event) {
        Map<String, Object> config = parseConfig(channel.config);
        String webhookUrl = (String) config.get("webhookUrl");
        String secret = (String) config.get("secret");

        if (webhookUrl == null || webhookUrl.isBlank()) {
            Log.warnf("DingTalk channel %s has no webhook URL", channel.id);
            return;
        }

        // TODO: 实现签名逻辑
        Map<String, Object> message = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "title", event.title,
                        "text", String.format("### %s\n\n**级别**: %s\n\n**类型**: %s\n\n**内容**: %s",
                                event.title, event.level, event.eventType, event.message)
                )
        );

        try {
            Response response = client.target(webhookUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(message));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("DingTalk notification failed: " + response.getStatus());
            }
            Log.infof("Sent DingTalk notification for event %s", event.id);
        } catch (Exception e) {
            Log.errorf(e, "Failed to send DingTalk notification");
            throw new RuntimeException(e);
        }
    }

    private void sendWebhook(AlertChannel channel, AlertEvent event) {
        Map<String, Object> config = parseConfig(channel.config);
        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "POST");

        if (url == null || url.isBlank()) {
            Log.warnf("Webhook channel %s has no URL", channel.id);
            return;
        }

        Map<String, Object> payload = Map.of(
                "eventId", event.id.toString(),
                "eventType", event.eventType.name(),
                "level", event.level.name(),
                "title", event.title,
                "message", event.message,
                "resourceId", event.resourceId != null ? event.resourceId.toString() : null,
                "resourceType", event.resourceType,
                "environmentId", event.environmentId != null ? event.environmentId.toString() : null,
                "createdAt", event.createdAt != null ? event.createdAt.toString() : null
        );

        try {
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .method(method, Entity.json(payload));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("Webhook notification failed: " + response.getStatus());
            }
            Log.infof("Sent Webhook notification for event %s", event.id);
        } catch (Exception e) {
            Log.errorf(e, "Failed to send Webhook notification");
            throw new RuntimeException(e);
        }
    }

    private void sendSms(AlertChannel channel, AlertEvent event) {
        // TODO: 实现短信发送
        Log.infof("Sending SMS notification for event %s via channel %s", event.id, channel.id);
    }

    private Map<String, Object> parseConfig(String config) {
        if (config == null || config.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(config, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            Log.errorf(e, "Failed to parse channel config: %s", config);
            return Map.of();
        }
    }

    private List<String> parseReceivers(String receivers) {
        if (receivers == null || receivers.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(receivers, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            Log.errorf(e, "Failed to parse receivers: %s", receivers);
            return List.of();
        }
    }
}