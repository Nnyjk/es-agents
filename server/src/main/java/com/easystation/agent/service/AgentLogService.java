package com.easystation.agent.service;

import com.easystation.agent.dto.AgentLogRecord;
import com.easystation.agent.dto.AgentLogRecord.LogEntry;
import com.easystation.agent.dto.AgentLogRecord.Query;
import com.easystation.agent.dto.AgentLogRecord.Stats;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AgentLogService {

    private static final String LOG_DIR = "work/logs";
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})(?:\\.\\d+)?\\s+(DEBUG|INFO|WARN|ERROR)\\s+(.+)$");

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
            String timestamp = LocalDateTime.now().format(DEFAULT_FORMATTER);
            String logLine = timestamp + " INFO " + content;
            Files.writeString(
                logFile, 
                logLine + System.lineSeparator(), 
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
            List<String> allLines = lines.collect(Collectors.toList());
            int fromIndex = Math.max(0, allLines.size() - limit);
            return allLines.subList(fromIndex, allLines.size());
        } catch (IOException e) {
            Log.errorf(e, "Failed to read logs for agent %s", agentId);
            return Collections.emptyList();
        }
    }

    public AgentLogRecord queryLogs(Query query) {
        Path logFile = getLogPath(query.agentId());
        if (!Files.exists(logFile)) {
            return new AgentLogRecord(query.agentId(), null, 0, Collections.emptyList());
        }

        try (Stream<String> lines = Files.lines(logFile, StandardCharsets.UTF_8)) {
            List<LogEntry> entries = new ArrayList<>();
            int lineNumber = 0;
            int totalCount = 0;

            for (String line : (Iterable<String>) lines::iterator) {
                lineNumber++;
                LogEntry entry = parseLogLine(line, lineNumber);
                if (entry == null) continue;

                // Apply filters
                totalCount++;
                if (query.level() != null && !entry.level().equalsIgnoreCase(query.level())) {
                    continue;
                }
                if (query.keyword() != null && !entry.message().contains(query.keyword())) {
                    continue;
                }

                entries.add(entry);
            }

            // Apply pagination
            int offset = query.offset() != null ? query.offset() : 0;
            int limit = query.limit() != null ? query.limit() : 100;
            int endIndex = Math.min(offset + limit, entries.size());
            
            List<LogEntry> pagedEntries = offset < entries.size() 
                ? entries.subList(offset, endIndex) 
                : Collections.emptyList();

            return new AgentLogRecord(
                query.agentId(), 
                null, 
                totalCount, 
                pagedEntries
            );
        } catch (IOException e) {
            Log.errorf(e, "Failed to query logs for agent %s", query.agentId());
            return new AgentLogRecord(query.agentId(), null, 0, Collections.emptyList());
        }
    }

    public Stats getLogStats(UUID agentId) {
        Path logFile = getLogPath(agentId);
        if (!Files.exists(logFile)) {
            return new Stats(0, 0, 0, 0, 0);
        }

        try (Stream<String> lines = Files.lines(logFile, StandardCharsets.UTF_8)) {
            int errorCount = 0;
            int warnCount = 0;
            int infoCount = 0;
            int debugCount = 0;

            for (String line : (Iterable<String>) lines::iterator) {
                LogEntry entry = parseLogLine(line, 0);
                if (entry == null) continue;

                switch (entry.level().toUpperCase()) {
                    case "ERROR" -> errorCount++;
                    case "WARN" -> warnCount++;
                    case "INFO" -> infoCount++;
                    case "DEBUG" -> debugCount++;
                }
            }

            int total = errorCount + warnCount + infoCount + debugCount;
            return new Stats(total, errorCount, warnCount, infoCount, debugCount);
        } catch (IOException e) {
            Log.errorf(e, "Failed to get log stats for agent %s", agentId);
            return new Stats(0, 0, 0, 0, 0);
        }
    }

    public List<String> getLatestLogs(UUID agentId, int lines) {
        return readLogs(agentId, lines);
    }

    private LogEntry parseLogLine(String line, int lineNumber) {
        if (line == null || line.isEmpty()) return null;

        Matcher matcher = LOG_LINE_PATTERN.matcher(line);
        if (matcher.matches()) {
            try {
                String timestamp = matcher.group(1);
                String level = matcher.group(2);
                String message = matcher.group(3);
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, DEFAULT_FORMATTER);
                return new LogEntry(lineNumber, dateTime, level, message);
            } catch (Exception e) {
                // Fallback: treat entire line as INFO
                return new LogEntry(lineNumber, null, "INFO", line);
            }
        }

        // Fallback: no timestamp format
        return new LogEntry(lineNumber, null, "INFO", line);
    }

    private Path getLogPath(UUID agentId) {
        return Paths.get(LOG_DIR, agentId.toString() + ".log");
    }
}