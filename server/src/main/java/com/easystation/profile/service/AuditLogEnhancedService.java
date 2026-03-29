package com.easystation.profile.service;

import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.dto.AuditLogRecord;
import com.easystation.profile.mapper.AuditLogMapper;
import com.easystation.profile.repository.AuditLogRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 审计日志服务增强版
 * 
 * 功能：
 * - 敏感操作自动标记
 * - 防篡改签名生成
 * - 异常行为检测
 */
@ApplicationScoped
public class AuditLogEnhancedService {

    @Inject
    AuditLogRepository auditLogRepository;

    @Inject
    AuditLogMapper auditLogMapper;

    // 签名密钥（生产环境应从配置中心获取）
    private static final String SIGNING_KEY = "esa-audit-signing-key-2026";

    /**
     * 记录审计日志（增强版）
     * 自动标记敏感操作、生成防篡改签名
     */
    @Transactional
    public AuditLogRecord logEnhanced(AuditLogRecord record) {
        UserAuditLog log = auditLogMapper.toEntity(record);

        // 1. 标记敏感操作
        log.isSensitive = UserAuditLog.isSensitiveAction(record.action, record.resourceType);
        
        // 2. 计算风险等级
        log.riskLevel = UserAuditLog.calculateRiskLevel(
            record.action, 
            record.resourceType, 
            log.isSensitive
        );

        // 3. 标记需要审查的日志
        if ("CRITICAL".equals(log.riskLevel) || "HIGH".equals(log.riskLevel)) {
            log.requiresReview = true;
            log.reviewStatus = "PENDING";
        }

        // 4. 生成内容哈希
        log.contentHash = generateContentHash(log);

        // 5. 生成防篡改签名
        log.integritySignature = generateIntegritySignature(log);

        // 6. 保存日志
        auditLogRepository.persist(log);

        Log.infof("Audit log created: userId=%s, action=%s, sensitive=%s, riskLevel=%s",
            log.userId, log.action, log.isSensitive, log.riskLevel);

        return auditLogMapper.toRecord(log);
    }

    /**
     * 验证审计日志完整性
     * 检查日志是否被篡改
     */
    public boolean verifyLogIntegrity(UserAuditLog log) {
        if (log.contentHash == null || log.integritySignature == null) {
            Log.warnf("Log %s missing integrity fields", log.id);
            return false;
        }

        // 验证内容哈希
        String currentHash = generateContentHash(log);
        if (!currentHash.equals(log.contentHash)) {
            Log.warnf("Log %s hash mismatch", log.id);
            return false;
        }

        // 验证签名
        String currentSignature = generateIntegritySignature(log);
        if (!currentSignature.equals(log.integritySignature)) {
            Log.warnf("Log %s signature mismatch", log.id);
            return false;
        }

        return true;
    }

    /**
     * 批量验证日志完整性
     */
    public Map<UUID, Boolean> verifyLogsIntegrity(List<UserAuditLog> logs) {
        Map<UUID, Boolean> results = new HashMap<>();
        for (UserAuditLog log : logs) {
            results.put(log.id, verifyLogIntegrity(log));
        }
        return results;
    }

    /**
     * 检测异常行为
     * 
     * 检测规则：
     * 1. 短时间内大量失败操作
     * 2. 非工作时间敏感操作
     * 3. 非常用 IP 地址访问
     * 4. 批量删除/修改操作
     */
    public List<AuditAnomaly> detectAnomalies(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditAnomaly> anomalies = new ArrayList<>();

        // 1. 检测短时间内大量失败操作
        anomalies.addAll(detectFailureSpike(userId, startTime, endTime));

        // 2. 检测非工作时间敏感操作
        anomalies.addAll(detectOffHoursSensitiveOps(userId, startTime, endTime));

        // 3. 检测非常用 IP
        anomalies.addAll(detectUnusualIP(userId, startTime, endTime));

        // 4. 检测批量操作
        anomalies.addAll(detectBatchOperations(userId, startTime, endTime));

        return anomalies;
    }

    /**
     * 检测失败操作激增
     */
    private List<AuditAnomaly> detectFailureSpike(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        // 统计每小时的失败次数
        List<Object[]> failureCounts = auditLogRepository.countFailuresByHour(userId, startTime, endTime);
        
        for (Object[] result : failureCounts) {
            LocalDateTime hour = (LocalDateTime) result[0];
            Long count = (Long) result[1];
            
            // 阈值：每小时失败超过 10 次
            if (count > 10) {
                AuditAnomaly anomaly = new AuditAnomaly();
                anomaly.type = "FAILURE_SPIKE";
                anomaly.severity = "HIGH";
                anomaly.userId = userId;
                anomaly.description = String.format("检测到 %d 次失败操作 (%s)", count, hour);
                anomaly.startTime = hour;
                anomaly.count = count;
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 检测非工作时间敏感操作
     */
    private List<AuditAnomaly> detectOffHoursSensitiveOps(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        List<UserAuditLog> sensitiveLogs = auditLogRepository.findSensitiveByUser(userId, startTime, endTime);
        
        for (UserAuditLog log : sensitiveLogs) {
            int hour = log.createdAt.getHour();
            // 非工作时间：22:00 - 06:00
            if (hour >= 22 || hour < 6) {
                AuditAnomaly anomaly = new AuditAnomaly();
                anomaly.type = "OFF_HOURS_SENSITIVE_OP";
                anomaly.severity = "MEDIUM";
                anomaly.userId = userId;
                anomaly.description = String.format("非工作时间敏感操作：%s at %s", log.action, log.createdAt);
                anomaly.startTime = log.createdAt;
                anomaly.logId = log.id;
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 检测非常用 IP 地址
     */
    private List<AuditAnomaly> detectUnusualIP(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        // 获取用户常用 IP（出现频率前 3）
        List<String> commonIPs = auditLogRepository.findCommonIPs(userId, 3);
        
        // 获取所有 IP
        List<UserAuditLog> allLogs = auditLogRepository.findByUserIdWithIP(userId, startTime, endTime);
        
        Set<String> unusualIPs = new HashSet<>();
        for (UserAuditLog log : allLogs) {
            if (log.ipAddress != null && !commonIPs.contains(log.ipAddress)) {
                unusualIPs.add(log.ipAddress);
            }
        }

        for (String ip : unusualIPs) {
            AuditAnomaly anomaly = new AuditAnomaly();
            anomaly.type = "UNUSUAL_IP";
            anomaly.severity = "MEDIUM";
            anomaly.userId = userId;
            anomaly.description = String.format("检测到非常用 IP: %s", ip);
            anomaly.metadata = new HashMap<>();
            anomaly.metadata.put("ip", ip);
            anomaly.metadata.put("commonIPs", commonIPs);
            anomalies.add(anomaly);
        }

        return anomalies;
    }

    /**
     * 检测批量操作
     */
    private List<AuditAnomaly> detectBatchOperations(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        List<UserAuditLog> batchLogs = auditLogRepository.findBatchOperations(userId, startTime, endTime);
        
        for (UserAuditLog log : batchLogs) {
            AuditAnomaly anomaly = new AuditAnomaly();
            anomaly.type = "BATCH_OPERATION";
            anomaly.severity = "HIGH";
            anomaly.userId = userId;
            anomaly.description = String.format("批量操作：%s on %s", log.action, log.resourceType);
            anomaly.startTime = log.createdAt;
            anomaly.logId = log.id;
            anomalies.add(anomaly);
        }

        return anomalies;
    }

    /**
     * 生成内容哈希
     */
    private String generateContentHash(UserAuditLog log) {
        try {
            String content = String.format("%s|%s|%s|%s|%s|%s",
                log.userId,
                log.action,
                log.resourceType != null ? log.resourceType : "",
                log.resourceId != null ? log.resourceId : "",
                log.status,
                log.createdAt
            );
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.error("Failed to generate content hash", e);
            return null;
        }
    }

    /**
     * 生成防篡改签名（HMAC-SHA256）
     */
    private String generateIntegritySignature(UserAuditLog log) {
        try {
            String content = log.contentHash + "|" + log.isSensitive + "|" + log.riskLevel;
            
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                SIGNING_KEY.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            sha256_HMAC.init(secretKey);
            
            byte[] signature = sha256_HMAC.doFinal(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : signature) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.error("Failed to generate integrity signature", e);
            return null;
        }
    }

    /**
     * 异常行为记录
     */
    public static class AuditAnomaly {
        public String type;
        public String severity; // LOW, MEDIUM, HIGH, CRITICAL
        public UUID userId;
        public String description;
        public LocalDateTime startTime;
        public Long count;
        public UUID logId;
        public Map<String, Object> metadata = new HashMap<>();
    }
}
