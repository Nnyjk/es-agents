package com.easystation.agent.event;

import com.easystation.agent.service.AgentTaskService;
import com.easystation.agent.service.CommandExecutionService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Handles task result events and updates task status.
 */
@ApplicationScoped
public class TaskResultEventHandler {

    @Inject
    AgentTaskService agentTaskService;

    @Inject
    CommandExecutionService commandExecutionService;

    public void onTaskResult(@Observes TaskResultEvent event) {
        Log.debugf("Received task result event for task %s", event.getTaskId());

        // Try to handle as AgentTask first
        try {
            agentTaskService.handleExecutionResult(
                    event.getTaskId(),
                    event.getStatus(),
                    event.getExitCode(),
                    event.getDurationMs(),
                    event.getOutput()
            );
        } catch (Exception e) {
            // If AgentTask handling fails, try CommandExecution
            Log.debugf("AgentTask handling failed, trying CommandExecution: %s", e.getMessage());
            try {
                commandExecutionService.handleExecutionResult(
                        event.getTaskId(),
                        event.getStatus(),
                        event.getExitCode(),
                        event.getDurationMs(),
                        event.getOutput()
                );
            } catch (Exception ex) {
                Log.warnf("Failed to handle task result for %s: %s", event.getTaskId(), ex.getMessage());
            }
        }
    }
}