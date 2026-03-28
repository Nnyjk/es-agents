package com.easystation.export.service;

import com.easystation.export.domain.ExportTask;
import com.easystation.export.dto.ExportRequest;
import com.easystation.export.dto.ExportTaskDTO;
import com.easystation.export.dto.ExportTaskListResponse;
import com.easystation.export.enums.ExportStatus;
import com.easystation.export.enums.ExportType;
import com.easystation.export.repository.ExportTaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ExportServiceTest {

    @Inject
    ExportService exportService;

    @Inject
    ExportTaskRepository exportTaskRepository;

    @Test
    void testCreateExportTask() {
        UUID userId = UUID.randomUUID();
        ExportRequest request = new ExportRequest(
            ExportType.EXCEL.name(),
            "DEPLOYMENT_HISTORY",
            null, null, null, null, null, null,
            1000, 0
        );

        UUID taskId = exportService.createExportTask(userId, request);

        assertNotNull(taskId);

        ExportTask task = exportTaskRepository.findById(taskId);
        assertNotNull(task);
        assertEquals(userId, task.userId);
        assertEquals(ExportType.EXCEL.name(), task.exportType);
        assertEquals("DEPLOYMENT_HISTORY", task.dataType);
        // Task starts as PENDING, may transition to PROCESSING or COMPLETED asynchronously
        assertTrue(task.status.equals(ExportStatus.PENDING.name()) || 
                   task.status.equals(ExportStatus.PROCESSING.name()) ||
                   task.status.equals(ExportStatus.COMPLETED.name()));
    }

    @Test
    void testGetTaskStatus() {
        UUID userId = UUID.randomUUID();
        ExportRequest request = new ExportRequest(
            ExportType.PDF.name(),
            "COMMAND_LOG",
            null, null, null, null, null, null,
            1000, 0
        );

        UUID taskId = exportService.createExportTask(userId, request);

        ExportTaskDTO taskDTO = exportService.getTaskStatus(taskId);
        assertNotNull(taskDTO);
        assertEquals(taskId, taskDTO.id());
        assertEquals(ExportType.PDF.name(), taskDTO.exportType());
        assertEquals("COMMAND_LOG", taskDTO.dataType());
    }

    @Test
    void testGetUserTasks() {
        UUID userId = UUID.randomUUID();
        
        // Create a few export tasks
        for (int i = 0; i < 3; i++) {
            ExportRequest request = new ExportRequest(
                ExportType.EXCEL.name(),
                "DEPLOYMENT_HISTORY",
                null, null, null, null, null, null,
                1000, 0
            );
            exportService.createExportTask(userId, request);
        }

        ExportTaskListResponse response = exportService.getUserTasks(userId);
        assertNotNull(response);
        assertTrue(response.total() >= 3);
        assertNotNull(response.tasks());
    }

    @Test
    void testGetTaskStatusNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        ExportTaskDTO taskDTO = exportService.getTaskStatus(nonExistentId);
        assertNull(taskDTO);
    }
}
