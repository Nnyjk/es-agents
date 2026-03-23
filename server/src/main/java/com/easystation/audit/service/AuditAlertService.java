package com.easystation.audit.service;

import com.easystation.audit.domain.AuditAlertConfig;
import com.easystation.audit.domain.AuditAlertHistory;
import com.easystation.audit.domain.AuditLog;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审计告警服务
 */
@Slf4j
@ApplicationScoped
public class AuditAlertService {

    @Inject
    RoutingContext routingContext;

    /**
     * 创建告警配置
     */
    @Transactional
    public AuditAlertConfig createConfig(AuditAlertConfig config) {
        // 检查名称是否重复
        if (AuditAlertConfig.find("name", config.name).firstResult() != null) {
            throw new IllegalArgumentException("告警配置名称已存在: " + config.name);
        }
        config.persist();
        return config;
    }

    /**
     * 更新告警配置
     */
    @Transactional
    public AuditAlertConfig updateConfig(UUID id, AuditAlertConfig update) {
        AuditAlertConfig config = AuditAlertConfig.findById(id);
        if (config == null) {
            throw new IllegalArgumentException("告警配置不存在: " + id);
        }

        // 检查名称是否重复
        if (!config.name.equals(update.name)) {
            if (AuditAlertConfig.find("name", update.name).firstResult() != null) {
                throw new IllegalArgumentException("告警配置名称已存在: " + update.name);
            }
        }

        config.name = update.name;
        config.description = update.description;
        config.alertType = update.alertType;
        config.sensitiveActions = update.sensitiveActions;
        config.whitelistUsers = update.whitelistUsers;
        config.failureThreshold = update.failureThreshold;
        config.timeWindowMinutes = update.timeWindowMinutes;
        config.notifyChannels = update.notifyChannels;
        config.enabled = update.enabled;

        return config;
    }

    /**
     * 删除告警配置
     */
    @Transactional
    public void deleteConfig(UUID id) {
        AuditAlertConfig config = AuditAlertConfig.findById(id);
        if (config == null) {
            throw new IllegalArgumentException("告警配置不存在: " + id);
        }
        config.delete();
    }

    /**
     * 获取告警配置
     */
    public Optional<AuditAlertConfig> getConfig(UUID id) {
        return AuditAlertConfig.findByIdOptional(id);
    }

    /**
     * 分页查询告警配置
     */
    public Map<String, Object> listConfigs(String alertType, Boolean enabled, int page, int size) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (alertType != null && !alertType.isEmpty()) {
            query.append(" and alertType = :alertType");
            params.put("alertType", alertType);
        }

        if (enabled != null) {
            query.append(" and enabled = :enabled");
            params.put("enabled", enabled);
        }

        PanacheQuery<AuditAlertConfig> panacheQuery = AuditAlertConfig.find(query.toString(), Sort.by("createdAt", Sort.Direction.Descending), params);
        List<AuditAlertConfig> list = panacheQuery.page(Page.of(page, size)).list();
        long total = panacheQuery.count();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 启用/禁用告警配置
     */
    @Transactional
    public void toggleConfig(UUID id, boolean enabled) {
        AuditAlertConfig config = AuditAlertConfig.findById(id);
        if (config == null) {
            throw new IllegalArgumentException("告警配置不存在: " + id);
        }
        config.enabled = enabled;
    }

    /**
     * 分页查询告警历史
     */
    public Map<String, Object> listHistory(String alertType, String status, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (alertType != null && !alertType.isEmpty()) {
            query.append(" and alertType = :alertType");
            params.put("alertType", alertType);
        }

        if (status != null && !status.isEmpty()) {
            query.append(" and status = :status");
            params.put("status", status);
        }

        if (startTime != null) {
            query.append(" and createdAt >= :startTime");
            params.put("startTime", startTime);
        }

        if (endTime != null) {
            query.append(" and createdAt <= :endTime");
            params.put("endTime", endTime);
        }

        PanacheQuery<AuditAlertHistory> panacheQuery = AuditAlertHistory.find(query.toString(), Sort.by("createdAt", Sort.Direction.Descending), params);
        List<AuditAlertHistory> list = panacheQuery.page(Page.of(page, size)).list();
        long total = panacheQuery.count();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 确认告警
     */
    @Transactional
    public void acknowledgeAlert(UUID id, String acknowledgedBy, String remark) {
        AuditAlertHistory alert = AuditAlertHistory.findById(id);
        if (alert == null) {
            throw new IllegalArgumentException("告警记录不存在: " + id);
        }

        alert.status = "ACKNOWLEDGED";
        alert.acknowledgedBy = acknowledgedBy;
        alert.acknowledgedAt = LocalDateTime.now();
        alert.remark = remark;
    }

    /**
     * 解决告警
     */
    @Transactional
    public void resolveAlert(UUID id, String remark) {
        AuditAlertHistory alert = AuditAlertHistory.findById(id);
        if (alert == null) {
            throw new IllegalArgumentException("告警记录不存在: " + id);
        }

        alert.status = "RESOLVED";
        alert.remark = remark;
    }

    /**
     * 定时检查告警条件
     */
    @Scheduled(every = "5m")
    @Transactional
    public void checkAlerts() {
        log.info("开始检查审计告警...");

        // 获取所有启用的告警配置
        List<AuditAlertConfig> configs = AuditAlertConfig.list("enabled", true);

        for (AuditAlertConfig config : configs) {
            try {
                switch (config.alertType) {
                    case "SENSITIVE_OPERATION":
                        checkSensitiveOperation(config);
                        break;
                    case "FAILED_OPERATION":
                        checkFailedOperation(config);
                        break;
                    case "ABNORMAL_IP":
                        checkAbnormalIp(config);
                        break;
                    case "FREQUENT_ACCESS":
                        checkFrequentAccess(config);
                        break;
                    default:
                        log.warn("未知的告警类型: {}", config.alertType);
                }
            } catch (Exception e) {
                log.error("检查告警配置[{}]时发生错误", config.name, e);
            }
        }

        log.info("审计告警检查完成");
    }

    /**
     * 检查敏感操作
     */
    private void checkSensitiveOperation(AuditAlertConfig config) {
        if (config.sensitiveActions == null || config.sensitiveActions.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkTime = now.minusMinutes(config.timeWindowMinutes != null ? config.timeWindowMinutes : 5);

        // 查询敏感操作
        for (AuditAction action : config.sensitiveActions) {
            List<AuditLog> logs = AuditLog.list(
                    "action = ?1 and createdAt >= ?2 and result = ?3",
                    Sort.by("createdAt", Sort.Direction.Descending),
                    action, checkTime, AuditResult.SUCCESS
            );

            // 过滤白名单用户
            logs = logs.stream()
                    .filter(log -> config.whitelistUsers == null || !config.whitelistUsers.contains(log.username))
                    .toList();

            if (!logs.isEmpty()) {
                // 创建告警历史
                createAlertHistory(config, "SENSITIVE_OPERATION", logs.get(0).username, logs.get(0).clientIp,
                        "检测到敏感操作: " + action.getDescription(), logs);
            }
        }
    }

    /**
     * 检查失败操作
     */
    private void checkFailedOperation(AuditAlertConfig config) {
        Integer threshold = config.failureThreshold != null ? config.failureThreshold : 5;
        Integer windowMinutes = config.timeWindowMinutes != null ? config.timeWindowMinutes : 5;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkTime = now.minusMinutes(windowMinutes);

        // 按用户分组统计失败操作
        List<AuditLog> failedLogs = AuditLog.list(
                "result = ?1 and createdAt >= ?2",
                Sort.by("createdAt", Sort.Direction.Descending),
                AuditResult.FAILED, checkTime
        );

        Map<String, List<AuditLog>> byUser = failedLogs.stream()
                .filter(log -> config.whitelistUsers == null || !config.whitelistUsers.contains(log.username))
                .collect(Collectors.groupingBy(log -> log.username));

        for (Map.Entry<String, List<AuditLog>> entry : byUser.entrySet()) {
            if (entry.getValue().size() >= threshold) {
                createAlertHistory(config, "FAILED_OPERATION", entry.getKey(), entry.getValue().get(0).clientIp,
                        "用户[" + entry.getKey() + "]在" + windowMinutes + "分钟内失败操作次数达到" + entry.getValue().size() + "次",
                        entry.getValue());
            }
        }
    }

    /**
     * 检查异常IP
     */
    private void checkAbnormalIp(AuditAlertConfig config) {
        // 这里可以实现更复杂的IP异常检测逻辑
        // 例如：检测新IP、检测来自特定地区的IP等
        log.debug("检查异常IP告警: {}", config.name);
    }

    /**
     * 检查频繁访问
     */
    private void checkFrequentAccess(AuditAlertConfig config) {
        Integer threshold = config.failureThreshold != null ? config.failureThreshold : 100;
        Integer windowMinutes = config.timeWindowMinutes != null ? config.timeWindowMinutes : 1;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkTime = now.minusMinutes(windowMinutes);

        // 按用户分组统计访问次数
        List<AuditLog> logs = AuditLog.list(
                "createdAt >= ?1",
                Sort.by("createdAt", Sort.Direction.Descending),
                checkTime
        );

        Map<String, List<AuditLog>> byUser = logs.stream()
                .filter(log -> config.whitelistUsers == null || !config.whitelistUsers.contains(log.username))
                .collect(Collectors.groupingBy(log -> log.username));

        for (Map.Entry<String, List<AuditLog>> entry : byUser.entrySet()) {
            if (entry.getValue().size() >= threshold) {
                createAlertHistory(config, "FREQUENT_ACCESS", entry.getKey(), entry.getValue().get(0).clientIp,
                        "用户[" + entry.getKey() + "]在" + windowMinutes + "分钟内访问次数达到" + entry.getValue().size() + "次",
                        entry.getValue().subList(0, Math.min(10, entry.getValue().size())));
            }
        }
    }

    /**
     * 创建告警历史
     */
    private void createAlertHistory(AuditAlertConfig config, String alertType, String triggerUser,
                                    String triggerIp, String detail, List<AuditLog> relatedLogs) {
        // 检查是否已存在相同的告警（5分钟内）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long existingCount = AuditAlertHistory.count(
                "configId = ?1 and triggerUser = ?2 and alertType = ?3 and createdAt >= ?4 and status = ?5",
                config.id, triggerUser, alertType, fiveMinutesAgo, "ACTIVE"
        );

        if (existingCount > 0) {
            log.info("告警已存在，跳过: configId={}, user={}", config.id, triggerUser);
            return;
        }

        AuditAlertHistory history = new AuditAlertHistory();
        history.configId = config.id;
        history.alertName = config.name;
        history.alertType = alertType;
        history.triggerUser = triggerUser;
        history.triggerIp = triggerIp;
        history.detail = detail;
        history.relatedRecordIds = relatedLogs.stream().map(log -> log.id).toList();
        history.notifyChannel = config.notifyChannels != null && !config.notifyChannels.isEmpty() 
                ? String.join(",", config.notifyChannels) : null;
        history.notifyStatus = "PENDING";

        history.persist();

        // 发送通知
        sendNotification(history, config);
    }

    /**
     * 发送通知
     */
    private void sendNotification(AuditAlertHistory history, AuditAlertConfig config) {
        if (config.notifyChannels == null || config.notifyChannels.isEmpty()) {
            history.notifyStatus = "FAILED";
            history.notifyError = "未配置通知渠道";
            return;
        }

        for (String channel : config.notifyChannels) {
            try {
                switch (channel.toLowerCase()) {
                    case "email":
                        sendEmailNotification(history);
                        break;
                    case "webhook":
                        sendWebhookNotification(history);
                        break;
                    case "slack":
                        sendSlackNotification(history);
                        break;
                    default:
                        log.warn("未知的通知渠道: {}", channel);
                }
                history.notifyStatus = "SENT";
            } catch (Exception e) {
                log.error("发送通知失败: channel={}", channel, e);
                history.notifyStatus = "FAILED";
                history.notifyError = e.getMessage();
            }
        }
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(AuditAlertHistory history) {
        // TODO: 实现邮件通知
        log.info("发送邮件通知: alertId={}, alertName={}", history.id, history.alertName);
    }

    /**
     * 发送Webhook通知
     */
    private void sendWebhookNotification(AuditAlertHistory history) {
        // TODO: 实现Webhook通知
        log.info("发送Webhook通知: alertId={}, alertName={}", history.id, history.alertName);
    }

    /**
     * 发送Slack通知
     */
    private void sendSlackNotification(AuditAlertHistory history) {
        // TODO: 实现Slack通知
        log.info("发送Slack通知: alertId={}, alertName={}", history.id, history.alertName);
    }
}