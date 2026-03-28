package com.easystation.infra.service;

import com.easystation.infra.domain.Environment;
import com.easystation.infra.domain.Host;
import com.easystation.infra.domain.enums.HostStatus;
import com.easystation.infra.record.HostRecord;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class HostServiceTest {

    @Inject
    HostService hostService;

    private UUID testEnvId;

    @BeforeEach
    @Transactional
    void setup() {
        // Create a test environment
        Environment env = new Environment();
        env.setName("test-env-" + UUID.randomUUID());
        env.setCode("test-code-" + UUID.randomUUID().toString().substring(0, 8));
        env.persist();
        testEnvId = env.id;
    }

    @AfterEach
    @Transactional
    void cleanup() {
        Host.deleteAll();
        Environment.deleteAll();
    }

    @Test
    void testListHostsWithNoEnvironmentFilter() {
        // Create test hosts
        createTestHost("host-1", testEnvId);
        createTestHost("host-2", testEnvId);

        List<HostRecord> hosts = hostService.list(null);

        assertTrue(hosts.size() >= 2);
    }

    @Test
    @Transactional
    void testListHostsWithEnvironmentFilter() {
        // Create another environment
        Environment env2 = new Environment();
        env2.setName("test-env-2-" + UUID.randomUUID());
        env2.setCode("test-code-2-" + UUID.randomUUID().toString().substring(0, 8));
        env2.persist();
        UUID env2Id = env2.id;

        createTestHost("host-1", testEnvId);
        createTestHost("host-2", env2Id);

        List<HostRecord> hosts = hostService.list(testEnvId);

        assertEquals(1, hosts.size());
        assertEquals("host-1", hosts.get(0).name());
    }

    @Test
    void testGetHostById() {
        HostRecord created = createTestHost("test-host", testEnvId);
        UUID hostId = created.id();

        HostRecord found = hostService.get(hostId);

        assertNotNull(found);
        assertEquals("test-host", found.name());
        assertEquals(hostId, found.id());
    }

    @Test
    void testGetHostByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(Exception.class, () -> hostService.get(nonExistentId));
    }

    @Test
    @Transactional
    void testCreateHost() {
        HostRecord.Create dto = new HostRecord.Create(
            "test-identifier",
            "Test Host",
            "test-hostname",
            "Linux",
            "192.168.1.100",
            9090,
            testEnvId,
            "Test description",
            new ArrayList<>(),
            null,
            null
        );

        HostRecord created = hostService.create(dto);

        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("Test Host", created.name());
        assertEquals("Linux", created.os());
        assertEquals(HostStatus.UNCONNECTED, created.status());
    }

    @Test
    @Transactional
    void testCreateHostWithDuplicateIdentifier() {
        // Create first host with identifier
        HostRecord.Create dto1 = new HostRecord.Create(
            "duplicate-identifier",
            "Host 1",
            "host-1",
            "Linux",
            "192.168.1.100",
            9090,
            testEnvId,
            null,
            new ArrayList<>(),
            null,
            null
        );
        hostService.create(dto1);

        // Try to create second host with same identifier
        HostRecord.Create dto2 = new HostRecord.Create(
            "duplicate-identifier",
            "Host 2",
            "host-2",
            "Linux",
            "192.168.1.101",
            9090,
            testEnvId,
            null,
            new ArrayList<>(),
            null,
            null
        );

        assertThrows(Exception.class, () -> hostService.create(dto2));
    }

    @Test
    @Transactional
    void testUpdateHost() {
        HostRecord created = createTestHost("original-name", testEnvId);
        UUID hostId = created.id();

        HostRecord.Update dto = new HostRecord.Update(
            null,
            "updated-name",
            null,
            null,
            null,
            null,
            "Updated description",
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        HostRecord updated = hostService.update(hostId, dto);

        assertEquals("updated-name", updated.name());
        assertEquals("Updated description", updated.description());
    }

    @Test
    @Transactional
    void testDeleteHost() {
        HostRecord created = createTestHost("to-delete", testEnvId);
        UUID hostId = created.id();

        hostService.delete(hostId);

        assertNull(Host.findById(hostId));
    }

    @Test
    void testDeleteHostNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(Exception.class, () -> hostService.delete(nonExistentId));
    }

    // Helper method to create test host
    @Transactional
    HostRecord createTestHost(String name, UUID envId) {
        HostRecord.Create dto = new HostRecord.Create(
            UUID.randomUUID().toString(),
            name,
            name + "-hostname",
            "Linux",
            "192.168.1.100",
            9090,
            envId,
            null,
            new ArrayList<>(),
            null,
            null
        );
        return hostService.create(dto);
    }
}