package com.easystation.agent.planning.executor;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskExecutionLog;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.executor.impl.TaskExecutorImpl;
import com.easystation.agent.planning.repository.PlanningTaskExecutionLogRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import com.easystation.agent.planning.scheduler.TaskScheduler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 任务执行器单元测试
 */
@QuarkusTest
class TaskExecutorTest {

    @Inject
    TaskExecutor taskExecutor;

    @InjectMock
    PlanningTaskRepository taskRepository;

    @InjectMock
    PlanningTaskExecutionLogRepository logRepository;

    @InjectMock
    TaskScheduler taskScheduler;

    @BeforeEach
    void setup() {
        Mockito.reset(taskRepository, logRepository, taskScheduler);
    }

    /**
     * 测试执行空任务
     */
    @Test
    void testExecuteNullTask() {
        TaskExecutor.ExecutionResult result = taskExecutor.execute(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试执行不可执行的任务
     */
    @Test
    void testExecuteNonExecutableTask() {
        PlanningTask task = createTestTask("Non-executable", PlanningTaskStatus.RUNNING);
        task.id = UUID.randomUUID();

        when(taskScheduler.canExecute(task)).thenReturn(false);

        TaskExecutor.ExecutionResult result = taskExecutor.execute(task);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试执行不存在的任务 ID
     */
    @Test
    void testExecuteByNonExistentId() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(null);

        TaskExecutor.ExecutionResult result = taskExecutor.executeById(taskId);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试没有可执行任务
     */
    @Test
    void testExecuteNextNoTaskAvailable() {
        UUID goalId = UUID.randomUUID();

        when(taskScheduler.getNextExecutableTask(goalId)).thenReturn(Optional.empty());

        Optional<TaskExecutor.ExecutionResult> result = taskExecutor.executeNext(goalId);

        assertFalse(result.isPresent());
    }

    /**
     * 测试检查是否可重试
     */
    @Test
    void testCanRetry() {
        PlanningTask retryableTask = createTestTask("Retryable", PlanningTaskStatus.FAILED);
        retryableTask.retryCount = 1;
        retryableTask.maxRetryCount = 3;

        PlanningTask maxRetryTask = createTestTask("Max Retry", PlanningTaskStatus.FAILED);
        maxRetryTask.retryCount = 3;
        maxRetryTask.maxRetryCount = 3;

        PlanningTask completedTask = createTestTask("Completed", PlanningTaskStatus.COMPLETED);
        completedTask.retryCount = 0;
        completedTask.maxRetryCount = 3;

        assertTrue(taskExecutor.canRetry(retryableTask));
        assertFalse(taskExecutor.canRetry(maxRetryTask));
        assertFalse(taskExecutor.canRetry(completedTask));
    }

    /**
     * 测试获取执行日志 - 空
     */
    @Test
    void testGetExecutionLogsEmpty() {
        UUID taskId = UUID.randomUUID();
        when(logRepository.findByTaskIdOrderByCreatedAtDesc(taskId)).thenReturn(Collections.emptyList());

        List<PlanningTaskExecutionLog> logs = taskExecutor.getExecutionLogs(taskId);

        assertNotNull(logs);
        assertTrue(logs.isEmpty());
    }

    /**
     * 测试获取不存在的处理器
     */
    @Test
    void testGetNonExistentHandler() {
        Optional<TaskExecutor.TaskHandler> handler = taskExecutor.getHandler("NON_EXISTENT");

        assertFalse(handler.isPresent());
    }

    /**
     * 测试执行空任务列表
     */
    @Test
    void testExecuteEmptyTaskList() {
        List<TaskExecutor.ExecutionResult> results = taskExecutor.executeTasks(Collections.emptyList());

        assertNotNull(results);
        assertTrue(results.isEmpty());

        results = taskExecutor.executeTasks(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // Helper methods

    private PlanningTask createTestTask(String description, PlanningTaskStatus status) {
        PlanningTask task = new PlanningTask();
        task.description = description;
        task.status = status;
        task.priority = TaskPriority.NORMAL;
        task.depth = 0;
        task.retryCount = 0;
        task.maxRetryCount = 3;
        task.executorType = "DEFAULT";
        return task;
    }
}