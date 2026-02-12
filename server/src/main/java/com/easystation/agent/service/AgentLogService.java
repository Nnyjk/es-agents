package com.easystation.agent.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AgentLogService {

    private static final String LOG_DIR = "work/logs";

    public AgentLogService() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            Log.error("Failed to create log directory", e);
        }
    }

    public void appendLog(UUID agentId, String content) {
        if (content == null || content.isEmpty()) return;

        Path logFile = getLogPath(agentId);
        try {
            Files.writeString(
                logFile, 
                content + System.lineSeparator(), 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            Log.errorf(e, "Failed to write log for agent %s", agentId);
        }
    }

    public List<String> readLogs(UUID agentId, int limit) {
        Path logFile = getLogPath(agentId);
        if (!Files.exists(logFile)) {
            return Collections.emptyList();
        }

        try (Stream<String> lines = Files.lines(logFile, StandardCharsets.UTF_8)) {
            // Read all lines is expensive for large files, but for now we assume manageable size
            // A better approach for tailing is RandomAccessFile, but let's keep it simple first
            List<String> allLines = lines.collect(Collectors.toList());
            int fromIndex = Math.max(0, allLines.size() - limit);
            return allLines.subList(fromIndex, allLines.size());
        } catch (IOException e) {
            Log.errorf(e, "Failed to read logs for agent %s", agentId);
            return Collections.emptyList();
        }
    }

    private Path getLogPath(UUID agentId) {
        return Paths.get(LOG_DIR, agentId.toString() + ".log");
    }
}
