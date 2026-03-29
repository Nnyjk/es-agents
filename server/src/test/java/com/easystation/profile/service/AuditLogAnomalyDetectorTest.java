package com.easystation.profile.service;

import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.service.AuditLogEnhancedService.AuditAnomaly;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AuditLogAnomalyDetectorTest {

    @Inject
    AuditLogEnhancedService enhancedService;

    private UUID testUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        startTime = LocalDateTime.now().minusDays(7);
        endTime = LocalDateTime.now();
    }

    @Test
    void testDetectFrequentFailures() {
        // 创建多次失败登录记录
        for (int i = 0; i < 6; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "LOGIN_FAILED";
            log.status = "failure";
            log.ipAddress = "192.168.1.100";
            log.isSensitive = true;
            log.riskLevel = "MEDIUM";
            log.operationCategory = "LOGIN";
            log.createdAt = LocalDateTime.now().minusMinutes(i * 2);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到频繁失败登录
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("FREQUENT_FAILURE") || a.description.contains("失败")
        ));
    }

    @Test
    void testDetectMultipleIPs() {
        // 创建来自多个 IP 的登录记录
        String[] ips = {"192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"};
        for (int i = 0; i < ips.length; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "LOGIN";
            log.status = "success";
            log.ipAddress = ips[i];
            log.isSensitive = false;
            log.riskLevel = "LOW";
            log.operationCategory = "LOGIN";
            log.createdAt = LocalDateTime.now().minusHours(i);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到多 IP 登录
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("MULTIPLE_IP") || a.description.contains("IP")
        ));
    }

    @Test
    void testDetectHighRiskActivities() {
        // 创建高风险操作记录
        for (int i = 0; i < 3; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "PERMISSION_CHANGE";
            log.status = "success";
            log.isSensitive = true;
            log.riskLevel = "HIGH";
            log.operationCategory = "SECURITY";
            log.description = "Changed permission for user " + i;
            log.createdAt = LocalDateTime.now().minusHours(i);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到高风险活动
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("HIGH_RISK") || a.description.contains("风险")
        ));
    }

    @Test
    void testDetectSensitiveDataAccess() {
        // 创建敏感数据访问记录
        for (int i = 0; i < 5; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "EXPORT";
            log.resourceType = "user";
            log.status = "success";
            log.isSensitive = true;
            log.riskLevel = "MEDIUM";
            log.operationCategory = "DATA_ACCESS";
            log.description = "Exported user data";
            log.createdAt = LocalDateTime.now().minusMinutes(i * 10);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到敏感数据访问
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("SENSITIVE_ACCESS") || a.description.contains("敏感")
        ));
    }

    @Test
    void testNoAnomalies_NormalActivity() {
        // 创建正常活动记录
        for (int i = 0; i < 3; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "LOGIN";
            log.status = "success";
            log.ipAddress = "192.168.1.1";
            log.isSensitive = false;
            log.riskLevel = "LOW";
            log.operationCategory = "LOGIN";
            log.createdAt = LocalDateTime.now().minusDays(i);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 正常活动不应该有太多异常
        assertTrue(anomalies.size() <= 1);  // 可能有一些轻微的异常
    }

    @Test
    void testDetectAfterHoursActivity() {
        // 创建非工作时间活动记录（凌晨 3 点）
        for (int i = 0; i < 3; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "DATA_ACCESS";
            log.status = "success";
            log.isSensitive = true;
            log.riskLevel = "MEDIUM";
            log.operationCategory = "DATA_ACCESS";
            // 设置为凌晨 3 点
            log.createdAt = LocalDateTime.now().minusDays(i).withHour(3).withMinute(0);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到非工作时间活动
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("AFTER_HOURS") || a.description.contains("时间") || a.description.contains("凌晨")
        ));
    }

    @Test
    void testDetectBulkOperations() {
        // 创建批量操作记录
        for (int i = 0; i < 10; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "BULK_DELETE";
            log.status = "success";
            log.isSensitive = true;
            log.riskLevel = "HIGH";
            log.operationCategory = "DATA_MODIFY";
            log.description = "Deleted " + (i + 1) + " records";
            log.createdAt = LocalDateTime.now().minusMinutes(i);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertNotNull(anomalies);
        // 应该检测到批量操作
        assertTrue(anomalies.stream().anyMatch(a -> 
            a.type.contains("BULK") || a.description.contains("批量")
        ));
    }

    @Test
    void testAnomalyDetails() {
        // 创建异常活动
        for (int i = 0; i < 6; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "LOGIN_FAILED";
            log.status = "failure";
            log.ipAddress = "192.168.1.100";
            log.isSensitive = true;
            log.riskLevel = "MEDIUM";
            log.createdAt = LocalDateTime.now().minusMinutes(i);
            log.persist();
        }

        List<AuditAnomaly> anomalies = enhancedService.detectAnomalies(testUserId, startTime, endTime);

        assertFalse(anomalies.isEmpty());
        
        AuditAnomaly anomaly = anomalies.get(0);
        assertNotNull(anomaly.type);
        assertNotNull(anomaly.description);
        assertNotNull(anomaly.severity);
        assertNotNull(anomaly.userId);
        assertNotNull(anomaly.count);
    }
}
