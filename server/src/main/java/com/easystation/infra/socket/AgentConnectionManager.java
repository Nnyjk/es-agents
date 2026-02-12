package com.easystation.infra.socket;

import com.easystation.agent.service.AgentLogService;
import com.easystation.agent.websocket.ConsoleWebSocket;
import com.easystation.common.config.AgentConfig;
import com.easystation.agent.record.HeartbeatRequest;
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
import java.util.concurrent.ExecutorService;

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
            // Skip UNCONNECTED and EXCEPTION
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

    public void connect(Host host) {
        if (host.getGatewayUrl() == null) return;
        
        // Check if already connected
        if (sessions.containsKey(host.getId())) {
            Session existing = sessions.get(host.getId());
            if (existing.isOpen()) {
                Log.infof("Host %s is already connected. Skipping connect request.", host.getId());
                return;
            } else {
                sessions.remove(host.getId());
            }
        }
        
        if (!connecting.add(host.getId())) {
             Log.infof("Host %s is currently connecting. Skipping duplicate request.", host.getId());
             return;
        }

        try {
            executor.submit(() -> doConnectWithRetry(host));
        } finally {
            connecting.remove(host.getId());
        }
    }

    @ActivateRequestContext
    public void doConnectWithRetry(Host host) {
        // Construct WS URL
        String url = host.getGatewayUrl();
        String wsUrl = buildWsUrl(url);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig config = buildConfig(host);

        int maxRetries = agentConfig.retryCount();
        long interval = agentConfig.retryInterval();

        for (int i = 0; i <= maxRetries; i++) {
            try {
                Log.infof("Connecting to Agent at %s (Attempt %d/%d)", wsUrl, i + 1, maxRetries + 1);
                AgentClientEndpoint endpoint = new AgentClientEndpoint(
                        msg -> handleMessage(host.getId(), msg),
                        (session) -> handleClose(host.getId(), session)
                );
                Session session = container.connectToServer((Endpoint) endpoint, config, URI.create(wsUrl));
                sessions.put(host.getId(), session);

                updateHostStatus(host.getId(), HostStatus.ONLINE);
                Log.infof("Connected to host %s (Session: %s)", host.getId(), session.getId());
                return; // Success
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
        
        // Failed after retries
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

        return ClientEndpointConfig.Builder.create()
                .configurator(configurator)
                .build();
    }
    
    public void disconnect(UUID hostId) {
        Session session = sessions.remove(hostId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
    
    public void send(UUID hostId, String message) {
        Session session = sessions.get(hostId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            Log.errorf("Cannot send message, no active session for host: %s", hostId);
        }
    }

    private void handleMessage(UUID hostId, String message) {
        Log.debugf("Received from %s: %s", hostId, message);

        // Try to parse JSON
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();
            
            if ("LOG".equals(type)) {
                String content = root.path("content").asText();
                agentLogService.appendLog(hostId, content);
            } else if ("HEARTBEAT".equals(type)) {
                JsonNode content = root.path("content");
                try {
                    HeartbeatRequest hb = objectMapper.treeToValue(content, HeartbeatRequest.class);
                    updateHostStatus(hostId, HostStatus.ONLINE, hb.osType());
                } catch (Exception e) {
                    Log.errorf("Failed to parse heartbeat from %s: %s", hostId, e.getMessage());
                    updateHostStatus(hostId, HostStatus.ONLINE);
                }
                // Broadcast to console so UI updates
                consoleWebSocket.broadcastLog(hostId.toString(), message);
                return;
            }
        } catch (JsonProcessingException e) {
            // Not JSON or parse error, ignore for log storage but might be legacy heartbeat
        }

        // Heartbeat check or other logic
        if ("HEARTBEAT".equals(message)) {
             updateHostStatus(hostId, HostStatus.ONLINE);
             // Also broadcast heartbeat to console for UI
             consoleWebSocket.broadcastLog(hostId.toString(), "{\"type\":\"HEARTBEAT\",\"content\":\"" + LocalDateTime.now() + "\"}");
        } else {
             // Forward to ConsoleWebSocket
             consoleWebSocket.broadcastLog(hostId.toString(), message);
        }
    }
    
    private void handleClose(UUID hostId, Session closedSession) {
        sessions.computeIfPresent(hostId, (id, currentSession) -> {
            if (currentSession.getId().equals(closedSession.getId())) {
                Log.infof("Session closed for host %s (Session: %s)", hostId, closedSession.getId());
                // If closed unexpectedly, we mark as OFFLINE. 
                // scheduledReconnect will retry connection later (if not UNCONNECTED/EXCEPTION).
                updateHostStatus(hostId, HostStatus.OFFLINE);
                return null; // Remove from map
            } else {
                 Log.infof("Old session closed for host %s (Closed: %s, Current: %s). Ignoring.", hostId, closedSession.getId(), currentSession.getId());
                 return currentSession; // Keep current
            }
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
    
    // Periodic Reconnection Logic
    @Scheduled(every = "30m")
    @Transactional
    public void scheduledReconnect() {
        Log.info("Starting scheduled reconnection...");
        // Reconnect logic: Check ALL hosts
        List<Host> hosts = Host.listAll();
        
        for (Host host : hosts) {
            // Skip UNCONNECTED and EXCEPTION
            if (host.getStatus() == HostStatus.UNCONNECTED || host.getStatus() == HostStatus.EXCEPTION) {
                continue;
            }
            
            // For others (ONLINE, OFFLINE), we disconnect (if exists) and reconnect
            disconnect(host.getId());
            connect(host);
        }
    }
}
