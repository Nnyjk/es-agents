package com.easystation.audit.service;

import com.easystation.audit.domain.AuditLog;
import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuditLogServiceTest {

    @Inject
    AuditLogService auditLogService;

    private AuditRecord.Create testCreateRecord;

    @BeforeEach
    void setUp() {
        testCreateRecord = new AuditRecord.Create(
                "testuser",
                UUID.randomUUID(),
                AuditAction.LOGIN,
                AuditResult.SUCCESS,
                "Test login",
                "USER",
                UUID.randomUUID(),
                "Test login details",
                null,
                null,
                "192.168.1.100",
                "Mozilla/5.0",
                "/api/v1/auth/login",
                "POST",
                100L,
                null
        );
    }

    @Test
    void testCreateAuditLog() {
        AuditRecord.Detail created = auditLogService.create(testCreateRecord);
        
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("testuser", created.username());
        assertEquals(AuditAction.LOGIN, created.action());
        assertEquals(AuditResult.SUCCESS, created.result());
        assertEquals("Test login", created.description());
        assertNotNull(created.createdAt());
    }

    @Test
    void testCreateAuditLogWithAllFields() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        
        AuditRecord.Create fullRecord = new AuditRecord.Create(
                "fulltestuser",
                userId,
                AuditAction.CREATE_HOST,
                AuditResult.SUCCESS,
                "Created new host",
                "HOST",
                resourceId,
                "Host created successfully",
                "{\"name\":\"test-host\"}",
                "{\"id\":\"created-id\"}",
                "192.168.1.100",
                "Mozilla/5.0",
                "/api/v1/hosts",
                "POST",
                150L,
                null
        );
        
        AuditRecord.Detail created = auditLogService.create(fullRecord);
        
        assertNotNull(created);
        assertEquals("fulltestuser", created.username());
        assertEquals(userId, created.userId());
        assertEquals(AuditAction.CREATE_HOST, created.action());
        assertEquals("HOST", created.resourceType());
        assertEquals(resourceId, created.resourceId());
        assertEquals("{\"name\":\"test-host\"}", created.requestParams());
        assertEquals(150L, created.duration());
    }

    @Test
    void testCreateAuditLogWithFailedResult() {
        AuditRecord.Create failedRecord = new AuditRecord.Create(
                "faileduser",
                null,
                AuditAction.LOGIN_FAILED,
                AuditResult.FAILED,
                "Login failed - invalid credentials",
                "USER",
                null,
                "Invalid username or password",
                null,
                null,
                "192.168.1.100",
                "Mozilla/5.0",
                "/api/v1/auth/login",
                "POST",
                50L,
                "Invalid username or password"
        );
        
        AuditRecord.Detail created = auditLogService.create(failedRecord);
        
        assertNotNull(created);
        assertEquals(AuditAction.LOGIN_FAILED, created.action());
        assertEquals(AuditResult.FAILED, created.result());
        assertEquals("Invalid username or password", created.errorMessage());
    }

    @Test
    void testGetAuditLogById() {
        // First create a log
        AuditRecord.Detail created = auditLogService.create(testCreateRecord);
        
        // Then retrieve it
        AuditRecord.Detail retrieved = auditLogService.get(created.id());
        
        assertNotNull(retrieved);
        assertEquals(created.id(), retrieved.id());
        assertEquals(created.username(), retrieved.username());
        assertEquals(created.action(), retrieved.action());
    }

    @Test
    void testGetNonExistentAuditLog() {
        UUID nonExistentId = UUID.randomUUID();
        AuditRecord.Detail retrieved = auditLogService.get(nonExistentId);
        
        assertNull(retrieved);
    }

    @Test
    void testListAuditLogsNoFilters() {
        // Create some test data
        auditLogService.create(testCreateRecord);
        
        AuditRecord.Query query = new AuditRecord.Query(
                null, null, null, null, null, null, null, null, null, null, null
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        assertTrue(logs.size() >= 1);
    }

    @Test
    void testListAuditLogsByUsernameFilter() {
        // Create test data with specific username
        String uniqueUsername = "test_user_" + System.currentTimeMillis();
        AuditRecord.Create record = new AuditRecord.Create(
                uniqueUsername,
                null,
                AuditAction.LOGIN,
                AuditResult.SUCCESS,
                "Test",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        auditLogService.create(record);
        
        AuditRecord.Query query = new AuditRecord.Query(
                uniqueUsername, null, null, null, null, null, null, null, null, null, null
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        assertTrue(logs.stream().anyMatch(log -> log.username().equals(uniqueUsername)));
    }

    @Test
    void testListAuditLogsByActionFilter() {
        AuditRecord.Query query = new AuditRecord.Query(
                null, null, AuditAction.LOGIN, null, null, null, null, null, null, null, null
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        // All returned logs should have LOGIN action
        logs.forEach(log -> assertEquals(AuditAction.LOGIN, log.action()));
    }

    @Test
    void testListAuditLogsByResultFilter() {
        AuditRecord.Query query = new AuditRecord.Query(
                null, null, null, AuditResult.SUCCESS, null, null, null, null, null, null, null
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        // All returned logs should have SUCCESS result
        logs.forEach(log -> assertEquals(AuditResult.SUCCESS, log.result()));
    }

    @Test
    void testListAuditLogsByResourceTypeFilter() {
        AuditRecord.Query query = new AuditRecord.Query(
                null, null, null, null, "USER", null, null, null, null, null, null
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        // All returned logs should have USER resource type
        logs.forEach(log -> {
            if (log.resourceType() != null) {
                assertEquals("USER", log.resourceType());
            }
        });
    }

    @Test
    void testListAuditLogsPagination() {
        AuditRecord.Query query = new AuditRecord.Query(
                null, null, null, null, null, null, null, null, null, 1, 10
        );
        
        List<AuditRecord.Detail> logs = auditLogService.list(query);
        
        assertNotNull(logs);
        assertTrue(logs.size() <= 10);
    }

    @Test
    void testDeleteAuditLog() {
        // First create a log
        AuditRecord.Detail created = auditLogService.create(testCreateRecord);
        
        // Then delete it
        boolean deleted = auditLogService.delete(created.id());
        
        assertTrue(deleted);
        
        // Verify it's deleted
        AuditRecord.Detail retrieved = auditLogService.get(created.id());
        assertNull(retrieved);
    }

    @Test
    void testDeleteNonExistentAuditLog() {
        UUID nonExistentId = UUID.randomUUID();
        boolean deleted = auditLogService.delete(nonExistentId);
        
        assertFalse(deleted);
    }

    @Test
    void testAuditLogWithPartialResult() {
        AuditRecord.Create partialRecord = new AuditRecord.Create(
                "batchuser",
                null,
                AuditAction.EXECUTE_COMMAND,
                AuditResult.PARTIAL,
                "Batch command execution - partial success",
                "COMMAND",
                null,
                "5/10 commands succeeded",
                null,
                null,
                null,
                null,
                null,
                null,
                5000L,
                null
        );
        
        AuditRecord.Detail created = auditLogService.create(partialRecord);
        
        assertNotNull(created);
        assertEquals(AuditResult.PARTIAL, created.result());
        assertEquals("5/10 commands succeeded", created.details());
    }
}
