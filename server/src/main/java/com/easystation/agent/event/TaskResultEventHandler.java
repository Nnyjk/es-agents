package com.easystation.agent.event;

import com.easystation.agent.service.AgentTaskService;
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

    public void onTaskResult(@Observes TaskResultEvent event) {
        Log.debugf("Received task result event for task %s", event.getTaskId());
        agentTaskService.handleExecutionResult(
                event.getTaskId(),
                event.getStatus(),
                event.getExitCode(),
                event.getDurationMs(),
                event.getOutput()
        );
    }
}