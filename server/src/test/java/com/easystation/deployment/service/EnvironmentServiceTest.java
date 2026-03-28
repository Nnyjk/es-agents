package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentEnvironment;
import com.easystation.deployment.dto.EnvironmentDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.enums.EnvironmentType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EnvironmentServiceTest {

    @Inject
    EnvironmentService environmentService;

    @BeforeEach
    @Transactional
    void setup() {
        DeploymentEnvironment.deleteAll();
    }

    @AfterEach
    @Transactional
    void cleanup() {
        DeploymentEnvironment.deleteAll();
    }

    @Test
    void testListEnvironmentsEmpty() {
        PageResultDTO<EnvironmentDTO> result = environmentService.listEnvironments(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @Transactional
    void testListEnvironmentsWithData() {
        createTestEnvironment("env-1", EnvironmentType.DEV);
        createTestEnvironment("env-2", EnvironmentType.PRODUCTION);

        PageResultDTO<EnvironmentDTO> result = environmentService.listEnvironments(1, 10, null, null, null);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getData().size());
    }

    @Test
    @Transactional
    void testListEnvironmentsWithNameFilter() {
        createTestEnvironment("dev-environment", EnvironmentType.DEV);
        createTestEnvironment("prod-environment", EnvironmentType.PRODUCTION);

        PageResultDTO<EnvironmentDTO> result = environmentService.listEnvironments(1, 10, "dev", null, null);

        assertEquals(1, result.getTotal());
        assertEquals("dev-environment", result.getData().get(0).getName());
    }

    @Test
    @Transactional
    void testListEnvironmentsWithTypeFilter() {
        createTestEnvironment("dev-env", EnvironmentType.DEV);
        createTestEnvironment("prod-env", EnvironmentType.PRODUCTION);

        PageResultDTO<EnvironmentDTO> result = environmentService.listEnvironments(1, 10, null, EnvironmentType.PRODUCTION, null);

        assertEquals(1, result.getTotal());
        assertEquals("prod-env", result.getData().get(0).getName());
        assertEquals(EnvironmentType.PRODUCTION, result.getData().get(0).getEnvironmentType());
    }

    @Test
    @Transactional
    void testGetEnvironmentById() {
        EnvironmentDTO created = createTestEnvironment("test-env", EnvironmentType.DEV);
        UUID envId = created.getId();

        EnvironmentDTO found = environmentService.getEnvironment(envId);

        assertNotNull(found);
        assertEquals("test-env", found.getName());
        assertEquals(envId, found.getId());
    }

    @Test
    void testGetEnvironmentByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> environmentService.getEnvironment(nonExistentId));
    }

    @Test
    @Transactional
    void testCreateEnvironment() {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName("new-environment");
        dto.setEnvironmentType(EnvironmentType.TEST);
        dto.setDescription("Test environment description");
        dto.setActive(true);

        EnvironmentDTO created = environmentService.createEnvironment(dto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("new-environment", created.getName());
        assertEquals(EnvironmentType.TEST, created.getEnvironmentType());
        assertTrue(created.getActive());
    }

    @Test
    @Transactional
    void testCreateEnvironmentWithDefaults() {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName("default-env");

        EnvironmentDTO created = environmentService.createEnvironment(dto);

        assertEquals(EnvironmentType.DEV, created.getEnvironmentType()); // Default type
        assertTrue(created.getActive()); // Default active
    }

    @Test
    @Transactional
    void testUpdateEnvironment() {
        EnvironmentDTO created = createTestEnvironment("original-name", EnvironmentType.DEV);
        UUID envId = created.getId();

        EnvironmentDTO updateDto = new EnvironmentDTO();
        updateDto.setName("updated-name");
        updateDto.setEnvironmentType(EnvironmentType.PRODUCTION);
        updateDto.setDescription("Updated description");

        EnvironmentDTO updated = environmentService.updateEnvironment(envId, updateDto);

        assertEquals("updated-name", updated.getName());
        assertEquals(EnvironmentType.PRODUCTION, updated.getEnvironmentType());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    @Transactional
    void testUpdateEnvironmentNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName("test");

        assertThrows(IllegalArgumentException.class, () -> environmentService.updateEnvironment(nonExistentId, dto));
    }

    @Test
    @Transactional
    void testDeleteEnvironment() {
        EnvironmentDTO created = createTestEnvironment("to-delete", EnvironmentType.DEV);
        UUID envId = created.getId();

        environmentService.deleteEnvironment(envId);

        assertNull(DeploymentEnvironment.findById(envId));
    }

    @Test
    @Transactional
    void testDeleteEnvironmentNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> environmentService.deleteEnvironment(nonExistentId));
    }

    @Test
    void testGetEnvironmentResources() {
        EnvironmentDTO env = createTestEnvironment("test-env", EnvironmentType.DEV);

        var resources = environmentService.getEnvironmentResources(env.getId());

        assertNotNull(resources);
        assertFalse(resources.isEmpty());
    }

    @Test
    void testGetEnvironmentApplications() {
        EnvironmentDTO env = createTestEnvironment("test-env", EnvironmentType.DEV);

        var applications = environmentService.getEnvironmentApplications(env.getId());

        assertNotNull(applications);
    }

    // Helper method to create test environment
    @Transactional
    EnvironmentDTO createTestEnvironment(String name, EnvironmentType type) {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName(name);
        dto.setEnvironmentType(type);
        dto.setActive(true);
        return environmentService.createEnvironment(dto);
    }
}