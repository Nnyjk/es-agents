package com.easystation.profile.service;

import com.easystation.profile.domain.UserAuditLog;
import com.easystation.profile.repository.AuditLogRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AuditLogExportServiceTest {

    @Inject
    AuditLogExportService exportService;

    @Inject
    AuditLogRepository auditLogRepository;

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
    @Transactional
    void testExportToCSV() {
        // 创建测试数据
        createTestAuditLogs(5);

        String csv = exportService.exportToCSV(testUserId, startTime, endTime, 100);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,User ID,Action,Resource Type"));
        assertTrue(csv.contains("LOGIN"));
        assertTrue(csv.contains("test_action"));
        
        // 验证行数（标题行 + 5 条数据）
        String[] lines = csv.split("\n");
        assertEquals(6, lines.length);
    }

    @Test
    @Transactional
    void testExportToCSV_Escaping() {
        // 创建包含特殊字符的测试数据
        UserAuditLog log = new UserAuditLog();
        log.userId = testUserId;
        log.action = "test,action";  // 包含逗号
        log.resourceType = "test\"resource";  // 包含引号
        log.status = "success";
        log.ipAddress = "192.168.1.1";
        log.isSensitive = false;
        log.riskLevel = "LOW";
        log.createdAt = LocalDateTime.now();
        log.persist();

        String csv = exportService.exportToCSV(testUserId, startTime, endTime, 100);

        assertNotNull(csv);
        // CSV 应该正确转义特殊字符
        assertTrue(csv.contains("\"test,action\""));
    }

    @Test
    @Transactional
    void testExportToJSON() {
        // 创建测试数据
        createTestAuditLogs(3);

        String json = exportService.exportToJSON(testUserId, startTime, endTime, 100);

        assertNotNull(json);
        assertTrue(json.contains("\"exportTime\""));
        assertTrue(json.contains("\"userId\""));
        assertTrue(json.contains("\"logs\""));
        assertTrue(json.contains("\"count\": 3"));
        assertTrue(json.contains("\"action\": \"LOGIN\""));
    }

    @Test
    @Transactional
    void testExportToJSON_Empty() {
        UUID emptyUserId = UUID.randomUUID();
        String json = exportService.exportToJSON(emptyUserId, startTime, endTime, 100);

        assertNotNull(json);
        assertTrue(json.contains("\"count\": 0"));
        assertTrue(json.contains("\"logs\": ["));
    }

    @Test
    @Transactional
    void testExportToGzipCSV() throws Exception {
        // 创建测试数据
        createTestAuditLogs(5);

        byte[] gzipData = exportService.exportToGzipCSV(testUserId, startTime, endTime, 100);

        assertNotNull(gzipData);
        assertTrue(gzipData.length > 0);
        // GZIP 文件头应该是 0x1f8b
        assertEquals(0x1f, gzipData[0] & 0xFF);
        assertEquals(0x8b, gzipData[1] & 0xFF);
    }

    @Test
    @Transactional
    void testGenerateSummary() {
        // 创建混合测试数据
        createTestAuditLogs(10);

        // 添加一些敏感日志
        for (int i = 0; i < 3; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "PASSWORD_CHANGE";
            log.status = "success";
            log.isSensitive = true;
            log.riskLevel = "HIGH";
            log.createdAt = LocalDateTime.now();
            log.persist();
        }

        // 添加一些失败日志
        for (int i = 0; i < 2; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = "LOGIN_FAILED";
            log.status = "failure";
            log.ipAddress = "192.168.1." + (100 + i);
            log.isSensitive = true;
            log.riskLevel = "MEDIUM";
            log.createdAt = LocalDateTime.now();
            log.persist();
        }

        AuditLogExportService.AuditReportSummary summary = 
            exportService.generateSummary(testUserId, startTime, endTime);

        assertNotNull(summary);
        assertTrue(summary.totalLogs >= 15);  // 10 + 3 + 2
        assertEquals(3, summary.sensitiveLogs);
        assertTrue(summary.failureLogs >= 2);
        assertTrue(summary.highRiskLogs >= 3);
        assertTrue(summary.uniqueIPs >= 1);
        assertNotNull(summary.topActions);
    }

    @Test
    @Transactional
    void testGenerateSummary_NoData() {
        UUID emptyUserId = UUID.randomUUID();
        AuditLogExportService.AuditReportSummary summary = 
            exportService.generateSummary(emptyUserId, startTime, endTime);

        assertNotNull(summary);
        assertEquals(0, summary.totalLogs);
        assertEquals(0, summary.sensitiveLogs);
        assertEquals(0, summary.failureLogs);
        assertEquals(0, summary.highRiskLogs);
        assertEquals(0, summary.uniqueIPs);
        assertEquals("", summary.topActions);
    }

    @Test
    @Transactional
    void testExportWithLimit() {
        // 创建 20 条测试数据
        createTestAuditLogs(20);

        // 限制只导出 10 条
        String csv = exportService.exportToCSV(testUserId, startTime, endTime, 10);

        String[] lines = csv.split("\n");
        assertEquals(11, lines.length);  // 标题行 + 10 条数据
    }

    private void createTestAuditLogs(int count) {
        for (int i = 0; i < count; i++) {
            UserAuditLog log = new UserAuditLog();
            log.userId = testUserId;
            log.action = i % 2 == 0 ? "LOGIN" : "test_action";
            log.resourceType = i % 3 == 0 ? "user" : "plugin";
            log.status = "success";
            log.ipAddress = "192.168.1." + (i + 1);
            log.isSensitive = false;
            log.riskLevel = "LOW";
            log.operationCategory = i % 4 == 0 ? "LOGIN" : "DATA_ACCESS";
            log.durationMs = (long)(100 + i * 10);
            log.description = "Test log " + i;
            log.createdAt = LocalDateTime.now().minusHours(i);
            log.persist();
        }
    }
}
