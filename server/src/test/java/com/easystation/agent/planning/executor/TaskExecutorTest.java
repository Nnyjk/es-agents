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
import static org.mockito.Mockito.when;

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
     * 测试成功执行任务
     */
    @Test
    void testExecuteSuccess() {
        PlanningTask task = createTestTask("Test Task", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();
        task.executorType = "DEFAULT";

        when(taskScheduler.canExecute(task)).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ExecutionResult result = taskExecutor.execute(task);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(task.id, result.getTaskId());
        assertTrue(result.getDurationMillis() >= 0);
    }

    /**
     * 测试执行不可执行的任务
     */
    @Test
    void testExecuteNonExecutableTask() {
        PlanningTask task = createTestTask("Non-executable", PlanningTaskStatus.RUNNING);
        task.id = UUID.randomUUID();

        when(taskScheduler.canExecute(task)).thenReturn(false);

        ExecutionResult result = taskExecutor.execute(task);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试执行空任务
     */
    @Test
    void testExecuteNullTask() {
        ExecutionResult result = taskExecutor.execute(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试通过 ID 执行任务
     */
    @Test
    void testExecuteById() {
        PlanningTask task = createTestTask("Task by ID", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();
        task.executorType = "DEFAULT";

        when(taskRepository.findById(task.id)).thenReturn(task);
        when(taskScheduler.canExecute(task)).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ExecutionResult result = taskExecutor.executeById(task.id);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试执行不存在的任务 ID
     */
    @Test
    void testExecuteByNonExistentId() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(null);

        ExecutionResult result = taskExecutor.executeById(taskId);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试执行下一个任务（目标范围）
     */
    @Test
    void testExecuteNextForGoal() {
        UUID goalId = UUID.randomUUID();
        PlanningTask task = createTestTask("Next Task", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();
        task.executorType = "DEFAULT";

        when(taskScheduler.getNextExecutableTask(goalId)).thenReturn(Optional.of(task));
        when(taskScheduler.canExecute(task)).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        Optional<ExecutionResult> result = taskExecutor.executeNext(goalId);

        assertTrue(result.isPresent());
        assertTrue(result.get().isSuccess());
    }

    /**
     * 测试没有可执行任务
     */
    @Test
    void testExecuteNextNoTaskAvailable() {
        UUID goalId = UUID.randomUUID();

        when(taskScheduler.getNextExecutableTask(goalId)).thenReturn(Optional.empty());

        Optional<ExecutionResult> result = taskExecutor.executeNext(goalId);

        assertFalse(result.isPresent());
    }

    /**
     * 测试开始执行
     */
    @Test
    void testStartExecution() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();

        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTask started = taskExecutor.startExecution(task);

        assertNotNull(started);
        assertEquals(PlanningTaskStatus.RUNNING, started.status);
        assertNotNull(started.startedAt);
    }

    /**
     * 测试完成执行
     */
    @Test
    void testCompleteExecution() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.RUNNING);
        task.id = UUID.randomUUID();
        task.startedAt = java.time.LocalDateTime.now().minusSeconds(5);

        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTask completed = taskExecutor.completeExecution(task, "Success result");

        assertNotNull(completed);
        assertEquals(PlanningTaskStatus.COMPLETED, completed.status);
        assertEquals("Success result", completed.result);
        assertNotNull(completed.completedAt);
        assertNotNull(completed.actualDurationSeconds);
    }

    /**
     * 测试失败执行
     */
    @Test
    void testFailExecution() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.RUNNING);
        task.id = UUID.randomUUID();
        task.startedAt = java.time.LocalDateTime.now().minusSeconds(3);

        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTask failed = taskExecutor.failExecution(task, "Error occurred");

        assertNotNull(failed);
        assertEquals(PlanningTaskStatus.FAILED, failed.status);
        assertEquals("Error occurred", failed.errorMessage);
        assertNotNull(failed.completedAt);
    }

    /**
     * 测试取消执行
     */
    @Test
    void testCancelExecution() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();

        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTask cancelled = taskExecutor.cancelExecution(task);

        assertNotNull(cancelled);
        assertEquals(PlanningTaskStatus.CANCELLED, cancelled.status);
        assertNotNull(cancelled.completedAt);
    }

    /**
     * 测试重试任务
     */
    @Test
    void testRetryTask() {
        PlanningTask task = createTestTask("Failed Task", PlanningTaskStatus.FAILED);
        task.id = UUID.randomUUID();
        task.retryCount = 1;
        task.maxRetryCount = 3;

        when(taskRepository.findById(task.id)).thenReturn(task);
        when(taskScheduler.rescheduleFailedTask(task.id)).thenReturn(true);
        when(taskScheduler.canExecute(any(PlanningTask.class))).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            PlanningTask t = invocation.getArgument(0);
            if (t.status == PlanningTaskStatus.RETRYING) {
                t.retryCount = 2;
            }
            return t;
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ExecutionResult result = taskExecutor.retry(task.id);

        assertNotNull(result);
        assertEquals(2, result.getRetryAttempt());
    }

    /**
     * 测试重试不可重试的任务
     */
    @Test
    void testRetryNonRetryableTask() {
        PlanningTask task = createTestTask("Max Retry", PlanningTaskStatus.FAILED);
        task.id = UUID.randomUUID();
        task.retryCount = 3;
        task.maxRetryCount = 3;

        when(taskRepository.findById(task.id)).thenReturn(task);

        ExecutionResult result = taskExecutor.retry(task.id);

        assertNotNull(result);
        assertFalse(result.isSuccess());
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
     * 测试获取执行日志
     */
    @Test
    void testGetExecutionLogs() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.COMPLETED);
        task.id = UUID.randomUUID();

        PlanningTaskExecutionLog log1 = new PlanningTaskExecutionLog();
        log1.task = task;
        log1.toStatus = PlanningTaskStatus.RUNNING;
        log1.message = "Started";

        PlanningTaskExecutionLog log2 = new PlanningTaskExecutionLog();
        log2.task = task;
        log2.toStatus = PlanningTaskStatus.COMPLETED;
        log2.message = "Completed";

        when(logRepository.findByTaskIdOrderByCreatedAtDesc(task.id))
                .thenReturn(List.of(log2, log1));

        List<PlanningTaskExecutionLog> logs = taskExecutor.getExecutionLogs(task.id);

        assertNotNull(logs);
        assertEquals(2, logs.size());
    }

    /**
     * 测试记录执行日志
     */
    @Test
    void testLogExecution() {
        PlanningTask task = createTestTask("Task", PlanningTaskStatus.SCHEDULED);
        task.id = UUID.randomUUID();

        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        PlanningTaskExecutionLog log = taskExecutor.logExecution(task,
                PlanningTaskStatus.SCHEDULED, PlanningTaskStatus.RUNNING, "Test message");

        assertNotNull(log);
        assertEquals(task, log.task);
        assertEquals(PlanningTaskStatus.SCHEDULED, log.fromStatus);
        assertEquals(PlanningTaskStatus.RUNNING, log.toStatus);
        assertEquals("Test message", log.message);
    }

    /**
     * 测试获取执行统计
     */
    @Test
    void testGetStatistics() {
        UUID goalId = UUID.randomUUID();

        PlanningTask completed1 = createTestTask("Completed 1", PlanningTaskStatus.COMPLETED);
        completed1.id = UUID.randomUUID();
        completed1.actualDurationSeconds = 10L;

        PlanningTask completed2 = createTestTask("Completed 2", PlanningTaskStatus.COMPLETED);
        completed2.id = UUID.randomUUID();
        completed2.actualDurationSeconds = 20L;

        PlanningTask failed = createTestTask("Failed", PlanningTaskStatus.FAILED);
        failed.id = UUID.randomUUID();
        failed.retryCount = 2;

        PlanningTask scheduled = createTestTask("Scheduled", PlanningTaskStatus.SCHEDULED);
        scheduled.id = UUID.randomUUID();

        when(taskRepository.findByGoalId(goalId))
                .thenReturn(List.of(completed1, completed2, failed, scheduled));

        ExecutionStatistics stats = taskExecutor.getStatistics(goalId);

        assertNotNull(stats);
        assertEquals(goalId, stats.getGoalId());
        assertEquals(2, stats.getSuccessCount());
        assertEquals(1, stats.getFailureCount());
        assertEquals(2, stats.getRetryCount());
        assertEquals(30000, stats.getTotalDurationMillis());
        assertEquals(15000, stats.getAverageDurationMillis());
        assertEquals(66.67, stats.getSuccessRate(), 0.1);
    }

    /**
     * 测试注册处理器
     */
    @Test
    void testRegisterHandler() {
        TaskHandler handler = new TestHandler("TEST_HANDLER");

        taskExecutor.registerHandler("TEST", handler);

        Optional<TaskHandler> retrieved = taskExecutor.getHandler("TEST");

        assertTrue(retrieved.isPresent());
        assertEquals("TEST_HANDLER", retrieved.get().getName());
    }

    /**
     * 测试获取不存在的处理器
     */
    @Test
    void testGetNonExistentHandler() {
        Optional<TaskHandler> handler = taskExecutor.getHandler("NON_EXISTENT");

        assertFalse(handler.isPresent());
    }

    /**
     * 测试执行任务序列
     */
    @Test
    void testExecuteSequence() {
        UUID goalId = UUID.randomUUID();

        PlanningTask task1 = createTestTask("Task 1", PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Task 2", PlanningTaskStatus.SCHEDULED);
        PlanningTask task3 = createTestTask("Task 3", PlanningTaskStatus.SCHEDULED);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task3.id = UUID.randomUUID();
        task1.executorType = "DEFAULT";
        task2.executorType = "DEFAULT";
        task3.executorType = "DEFAULT";

        when(taskScheduler.getExecutionOrder(goalId)).thenReturn(List.of(task1, task2, task3));
        when(taskScheduler.canExecute(any(PlanningTask.class))).thenReturn(true);
        when(taskScheduler.areDependenciesMet(any(UUID.class))).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        List<ExecutionResult> results = taskExecutor.executeSequence(goalId);

        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(ExecutionResult::isSuccess));
    }

    /**
     * 测试执行任务列表
     */
    @Test
    void testExecuteTasks() {
        PlanningTask task1 = createTestTask("Task 1", PlanningTaskStatus.SCHEDULED);
        PlanningTask task2 = createTestTask("Task 2", PlanningTaskStatus.SCHEDULED);

        task1.id = UUID.randomUUID();
        task2.id = UUID.randomUUID();
        task1.executorType = "DEFAULT";
        task2.executorType = "DEFAULT";

        when(taskScheduler.canExecute(any(PlanningTask.class))).thenReturn(true);
        when(taskRepository.persist(any(PlanningTask.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(logRepository.persist(any(PlanningTaskExecutionLog.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        List<ExecutionResult> results = taskExecutor.executeTasks(List.of(task1, task2));

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * 测试执行空任务列表
     */
    @Test
    void testExecuteEmptyTaskList() {
        List<ExecutionResult> results = taskExecutor.executeTasks(Collections.emptyList());

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

    /**
     * 测试处理器实现
     */
    private static class TestHandler implements TaskHandler {
        private final String name;

        TestHandler(String name) {
            this.name = name;
        }

        @Override
        public HandlerResult handle(PlanningTask task) {
            return HandlerResult.success("Handled by " + name);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}