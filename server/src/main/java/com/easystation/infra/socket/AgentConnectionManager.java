package com.easystation.infra.socket;

import com.easystation.agent.record.HeartbeatRequest;
import com.easystation.agent.service.AgentLogService;
import com.easystation.agent.websocket.ConsoleWebSocket;
import com.easystation.common.config.AgentConfig;
import com.easystation.infra.domain.Host;
import com.easystation.infra.domain.enums.HostStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class AgentConnectionManager {

    @Inject
    ConsoleWebSocket consoleWebSocket;

    @Inject
    AgentLogService agentLogService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AgentConfig agentConfig;

    @Inject
    @Named("agentConnectionExecutor")
    ExecutorService executor;

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Set<UUID> connecting = ConcurrentHashMap.newKeySet();

    void onStart(@Observes StartupEvent ev) {
        Log.info("Starting Agent connection check...");
        executor.submit(this::checkAndConnectAll);
    }

    @Transactional
    void checkAndConnectAll() {
        List<Host> hosts = Host.listAll();
        for (Host host : hosts) {
            if (host.getStatus() == HostStatus.UNCONNECTED || host.getStatus() == HostStatus.EXCEPTION) {
                continue;
            }
            connect(host);
        }
    }

    @PostConstruct
    void init() {
        Log.info("AgentConnectionManager initialized");
    }

    /**
     * Connect to HostAgent and wait for connection result
     * @param host Host to connect to
     * @param timeoutMs Timeout in milliseconds
     * @return true if connected successfully, false otherwise
     */
    public boolean connectAndWait(Host host, long timeoutMs) {
        if (host.getGatewayUrl() == null || host.getGatewayUrl().isBlank()) {
            Log.warnf("Skip connect for host %s: empty gatewayUrl", host.getId());
            return false;
        }

        // Check if already connected
        Session existing = sessions.get(host.getId());
        if (existing != null && existing.isOpen()) {
            Log.infof("Host %s is already connected.", host.getId());
            return true;
        }
        
        // Remove stale session
        if (existing != null) {
            sessions.remove(host.getId());
        }

        // Prevent duplicate connection attempts
        if (!connecting.add(host.getId())) {
            Log.infof("Host %s is currently connecting. Waiting...", host.getId());
            // Wait a bit for existing connection attempt
            try {
                Thread.sleep(Math.min(timeoutMs, 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            existing = sessions.get(host.getId());
            return existing != null && existing.isOpen();
        }

        try {
            return doConnectWithRetryAndWait(host, timeoutMs);
        } finally {
            connecting.remove(host.getId());
        }
    }

    /**
     * Asynchronous connect - initiates connection in background
     */
    public void connect(Host host) {
        if (host.getGatewayUrl() == null || host.getGatewayUrl().isBlank()) {
            Log.warnf("Skip connect for host %s: empty gatewayUrl", host.getId());
            return;
        }

        Session existing = sessions.get(host.getId());
        if (existing != null && existing.isOpen()) {
            Log.infof("Host %s is already connected. Skipping connect request.", host.getId());
            return;
        }
        if (existing != null) {
            sessions.remove(host.getId());
        }

        if (!connecting.add(host.getId())) {
            Log.infof("Host %s is currently connecting. Skipping duplicate request.", host.getId());
            return;
        }

        executor.submit(() -> {
            try {
                doConnectWithRetry(host);
            } finally {
                connecting.remove(host.getId());
            }
        });
    }

    @ActivateRequestContext
    public boolean doConnectWithRetryAndWait(Host host, long timeoutMs) {
        String wsUrl = buildWsUrl(host.getGatewayUrl());
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig config = buildConfig(host);

        long startTime = System.currentTimeMillis();
        long remainingTimeout = timeoutMs;
        
        // Try to connect with timeout
        try {
            Log.infof("Connecting to Agent at %s (timeout: %dms)", wsUrl, timeoutMs);
            
            CountDownLatch connectionLatch = new CountDownLatch(1);
            AtomicBoolean connectionSuccess = new AtomicBoolean(false);
            
            AgentClientEndpoint endpoint = new AgentClientEndpoint(
                    msg -> handleMessage(host.getId(), msg),
                    session -> handleClose(host.getId(), session),
                    (session, success) -> {
                        connectionSuccess.set(success);
                        connectionLatch.countDown();
                    }
            );
            
            // Connect in a separate thread to enforce timeout
            Thread connectThread = new Thread(() -> {
                try {
                    Session session = container.connectToServer((Endpoint) endpoint, config, URI.create(wsUrl));
                    sessions.put(host.getId(), session);
                    connectionSuccess.set(true);
                    Log.infof("Connected to host %s (Session: %s)", host.getId(), session.getId());
                } catch (Exception e) {
                    Log.errorf("Failed to connect to host %s: %s", host.getId(), e.getMessage());
                    connectionSuccess.set(false);
                } finally {
                    connectionLatch.countDown();
                }
            });
            
            connectThread.start();
            
            // Wait for connection with timeout
            if (!connectionLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                connectThread.interrupt();
                Log.errorf("Connection to host %s timed out after %dms", host.getId(), timeoutMs);
                updateHostStatus(host.getId(), HostStatus.EXCEPTION);
                return false;
            }
            
            if (connectionSuccess.get()) {
                updateHostStatus(host.getId(), HostStatus.ONLINE);
                return true;
            } else {
                updateHostStatus(host.getId(), HostStatus.EXCEPTION);
                return false;
            }
            
        } catch (Exception e) {
            Log.errorf("Failed to connect to host %s: %s", host.getId(), e.getMessage());
            updateHostStatus(host.getId(), HostStatus.EXCEPTION);
            return false;
        }
    }

    @ActivateRequestContext
    public void doConnectWithRetry(Host host) {
        String wsUrl = buildWsUrl(host.getGatewayUrl());
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig config = buildConfig(host);

        int maxRetries = agentConfig.retryCount();
        long interval = agentConfig.retryInterval();

        for (int i = 0; i <= maxRetries; i++) {
            try {
                Log.infof("Connecting to Agent at %s (Attempt %d/%d)", wsUrl, i + 1, maxRetries + 1);
                AgentClientEndpoint endpoint = new AgentClientEndpoint(
                        msg -> handleMessage(host.getId(), msg),
                        session -> handleClose(host.getId(), session),
                        (session, success) -> {
                            if (success) {
                                sessions.put(host.getId(), session);
                                updateHostStatus(host.getId(), HostStatus.ONLINE);
                                Log.infof("Connected to host %s (Session: %s)", host.getId(), session.getId());
                            }
                        }
                );
                Session session = container.connectToServer((Endpoint) endpoint, config, URI.create(wsUrl));
                
                // For async connect, we put session here too
                if (!sessions.containsKey(host.getId())) {
                    sessions.put(host.getId(), session);
                    updateHostStatus(host.getId(), HostStatus.ONLINE);
                    Log.infof("Connected to host %s (Session: %s)", host.getId(), session.getId());
                }
                return;
            } catch (Exception e) {
                Log.errorf("Failed to connect to host %s: %s", host.getId(), e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        updateHostStatus(host.getId(), HostStatus.EXCEPTION);
        Log.infof("Marking host %s as EXCEPTION after failed connection attempts.", host.getId());
    }

    private String buildWsUrl(String url) {
        String wsUrl;
        if (url.startsWith("http://")) {
            wsUrl = url.replace("http://", "ws://");
        } else if (url.startsWith("https://")) {
            wsUrl = url.replace("https://", "wss://");
        } else if (url.startsWith("ws://") || url.startsWith("wss://")) {
            wsUrl = url;
        } else {
            wsUrl = "ws://" + url;
        }

        if (!wsUrl.endsWith("/ws")) {
            wsUrl = wsUrl + (wsUrl.endsWith("/") ? "ws" : "/ws");
        }
        return wsUrl;
    }

    private ClientEndpointConfig buildConfig(Host host) {
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("X-Agent-Secret", Collections.singletonList(host.getSecretKey()));
            }
        };

        return ClientEndpointConfig.Builder.create().configurator(configurator).build();
    }

    public void disconnect(UUID hostId) {
        Session session = sessions.remove(hostId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                Log.debugf("ignore close error for host %s: %s", hostId, e.getMessage());
            }
        }
    }

    public boolean send(UUID hostId, String message) {
        Session session = sessions.get(hostId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
            try {
                JsonNode root = objectMapper.readTree(message);
                String requestId = root.path("requestId").asText("");
                String type = root.path("type").asText("UNKNOWN");
                Log.infof("Dispatched message to host=%s type=%s requestId=%s", hostId, type, requestId);
            } catch (Exception ignored) {
                Log.infof("Dispatched raw message to host=%s", hostId);
            }
            return true;
        }

        Log.errorf("Cannot send message, no active session for host: %s", hostId);
        return false;
    }

    private void handleMessage(UUID hostId, String message) {
        Log.debugf("Received from %s: %s", hostId, message);

        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();

            String requestId = root.path("requestId").asText("");
            if ("LOG".equals(type)) {
                String content = root.path("content").asText();
                agentLogService.appendLog(hostId, content);
                if (!requestId.isBlank()) {
                    Log.debugf("Log message host=%s requestId=%s", hostId, requestId);
                }
            } else if ("HEARTBEAT".equals(type)) {
                JsonNode content = root.path("content");
                try {
                    HeartbeatRequest hb = objectMapper.treeToValue(content, HeartbeatRequest.class);
                    updateHostStatus(hostId, HostStatus.ONLINE, hb.osType());
                } catch (Exception e) {
                    Log.errorf("Failed to parse heartbeat from %s: %s", hostId, e.getMessage());
                    updateHostStatus(hostId, HostStatus.ONLINE);
                }
                consoleWebSocket.broadcastLog(hostId.toString(), message);
                return;
            } else if ("EXEC_RESULT".equals(type)) {
                JsonNode content = root.path("content");
                String status = content.path("status").asText("UNKNOWN");
                int exitCode = content.path("exitCode").asInt(-1);
                long durationMs = content.path("durationMs").asLong(-1);
                String summary = String.format("EXEC_RESULT requestId=%s status=%s exitCode=%d durationMs=%d", requestId, status, exitCode, durationMs);
                agentLogService.appendLog(hostId, summary);
                Log.infof("Exec result received host=%s requestId=%s status=%s exitCode=%d durationMs=%d", hostId, requestId, status, exitCode, durationMs);
                consoleWebSocket.broadcastLog(hostId.toString(), message);
                return;
            }
        } catch (JsonProcessingException e) {
            // ignore parse errors for backward compatibility
        }

        if ("HEARTBEAT".equals(message)) {
            updateHostStatus(hostId, HostStatus.ONLINE);
            consoleWebSocket.broadcastLog(hostId.toString(), "{\"type\":\"HEARTBEAT\",\"content\":\"" + LocalDateTime.now() + "\"}");
        } else {
            consoleWebSocket.broadcastLog(hostId.toString(), message);
        }
    }

    private void handleClose(UUID hostId, Session closedSession) {
        sessions.computeIfPresent(hostId, (id, currentSession) -> {
            if (currentSession.getId().equals(closedSession.getId())) {
                Log.infof("Session closed for host %s (Session: %s)", hostId, closedSession.getId());
                updateHostStatus(hostId, HostStatus.OFFLINE);
                return null;
            }
            Log.infof("Old session closed for host %s (Closed: %s, Current: %s). Ignoring.", hostId, closedSession.getId(), currentSession.getId());
            return currentSession;
        });
    }

    @Transactional
    public void updateHostStatus(UUID hostId, HostStatus status, String osType) {
        Host host = Host.findById(hostId);
        if (host != null) {
            host.setStatus(status);
            if (status == HostStatus.ONLINE) {
                host.setLastHeartbeat(LocalDateTime.now());
                if (osType != null) {
                    host.setOs(osType);
                }
            }
            host.persist();
        }
    }

    @Transactional
    public void updateHostStatus(UUID hostId, HostStatus status) {
        updateHostStatus(hostId, status, null);
    }

    @Scheduled(every = "30m")
    @Transactional
    public void scheduledReconnect() {
        Log.info("Starting scheduled reconnection...");
        List<Host> hosts = Host.listAll();

        for (Host host : hosts) {
            if (host.getStatus() == HostStatus.UNCONNECTED || host.getStatus() == HostStatus.EXCEPTION) {
                continue;
            }
            disconnect(host.getId());
            connect(host);
        }
    }
}
