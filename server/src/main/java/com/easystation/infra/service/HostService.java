package com.easystation.infra.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.service.AgentSourceService;
import com.easystation.infra.domain.Environment;
import com.easystation.infra.domain.Host;
import com.easystation.infra.domain.enums.HostStatus;
import com.easystation.infra.record.HostInstallGuideRecord;
import com.easystation.infra.record.HostRecord;
import com.easystation.infra.socket.AgentConnectionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HostService {

    @Inject
    AgentSourceService agentSourceService;

    @Inject
    HostAgentResourceResolver resourceResolver;

    @Inject
    HostAgentPackageBuilder packageBuilder;

    @Inject
    AgentConnectionManager connectionManager;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Connect to HostAgent - performs actual connection test
     * @param id Host ID
     * @throws WebApplicationException if connection fails
     */
    public void connect(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        
        // Try to establish connection and wait for result
        boolean connected = connectionManager.connectAndWait(host, 5000);
        if (!connected) {
            throw new WebApplicationException(
                "Failed to connect to HostAgent. Please ensure the agent is running and accessible.", 
                Response.Status.BAD_GATEWAY
            );
        }
    }

    /**
     * Check host reachability using TCP connection
     * @param id Host ID
     * @return Host record with updated status
     */
    @Transactional
    public HostRecord checkReachability(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        
        boolean reachable = isHostReachable(host.hostname, host.port, 5000);
        HostStatus newStatus = reachable ? HostStatus.ONLINE : HostStatus.OFFLINE;
        
        host.status = newStatus;
        host.lastCheckedAt = LocalDateTime.now();
        host.persist();
        
        Log.infof("Host %s (%s:%d) reachability check: %s", host.name, host.hostname, host.port, newStatus);
        
        return toDto(host);
    }

    /**
     * Check reachability of all hosts
     * @return List of host records with updated status
     */
    @Transactional
    public List<HostRecord> checkReachabilityAll() {
        return Host.listAll().stream()
            .map(host -> {
                try {
                    return checkReachability(host.id);
                } catch (Exception e) {
                    Log.errorf("Failed to check host %s: %s", host.name, e.getMessage());
                    return toDto(host);
                }
            })
            .toList();
    }

    /**
     * Check if host is reachable using TCP connection
     * @param address Host address
     * @param port Host port
     * @param timeout Timeout in milliseconds
     * @return true if reachable, false otherwise
     */
    private boolean isHostReachable(String address, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), timeout);
            return true;
        } catch (IOException e) {
            Log.debugf("Host %s:%d is not reachable: %s", address, port, e.getMessage());
            return false;
        }
    }

    public List<HostRecord> list(UUID envId) {
        List<Host> entities = envId != null 
            ? Host.list("environment.id", envId)
            : Host.listAll();
        
        return entities.stream()
            .map(this::toDto)
            .toList();
    }

    public HostRecord get(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        return toDto(host);
    }

    @Transactional
    public HostRecord create(HostRecord.Create dto) {
        Environment env = Environment.findById(dto.environmentId());
        if (env == null) {
            throw new WebApplicationException("Environment not found", Response.Status.BAD_REQUEST);
        }

        // Generate or use provided identifier
        String identifier = dto.identifier();
        if (identifier == null || identifier.isBlank()) {
            identifier = UUID.randomUUID().toString();
        }

        // Check identifier uniqueness
        if (Host.count("identifier = ?1", identifier) > 0) {
            throw new WebApplicationException("Host with identifier '" + identifier + "' already exists", Response.Status.CONFLICT);
        }

        Host host = new Host();
        host.setIdentifier(identifier);
        host.setName(dto.name());
        host.setHostname(dto.hostname());
        host.setOs(dto.os());
        host.setIp(dto.ip());
        host.setPort(dto.port() != null ? dto.port() : 9090);
        host.setEnvironment(env);
        host.setDescription(dto.description());
        host.setTags(dto.tags() != null ? dto.tags() : new ArrayList<>());
        host.setEnabled(true);
        host.setStatus(HostStatus.UNCONNECTED);
        host.setSecretKey(UUID.randomUUID().toString());
        host.setGatewayUrl(dto.gatewayUrl());
        if (dto.listenPort() != null) host.setListenPort(dto.listenPort());

        host.persist();
        return toDto(host);
    }

    @Transactional
    public HostRecord update(UUID id, HostRecord.Update dto) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }

        // Check identifier uniqueness if being changed
        if (dto.identifier() != null && !dto.identifier().equals(host.getIdentifier())) {
            if (Host.count("identifier = ?1", dto.identifier()) > 0) {
                throw new WebApplicationException("Host with identifier '" + dto.identifier() + "' already exists", Response.Status.CONFLICT);
            }
            host.setIdentifier(dto.identifier());
        }

        if (dto.name() != null) host.setName(dto.name());
        if (dto.hostname() != null) host.setHostname(dto.hostname());
        if (dto.os() != null) host.setOs(dto.os());
        if (dto.ip() != null) host.setIp(dto.ip());
        if (dto.port() != null) host.setPort(dto.port());
        if (dto.description() != null) host.setDescription(dto.description());
        if (dto.tags() != null) host.setTags(dto.tags());
        if (dto.enabled() != null) host.setEnabled(dto.enabled());
        if (dto.config() != null) host.setConfig(dto.config());
        if (dto.heartbeatInterval() != null) host.setHeartbeatInterval(dto.heartbeatInterval());
        if (dto.gatewayUrl() != null) host.setGatewayUrl(dto.gatewayUrl());
        if (dto.listenPort() != null) host.setListenPort(dto.listenPort());

        if (dto.environmentId() != null) {
            Environment env = Environment.findById(dto.environmentId());
            if (env == null) {
                throw new WebApplicationException("Environment not found", Response.Status.BAD_REQUEST);
            }
            host.setEnvironment(env);
        }

        host.persist();
        return toDto(host);
    }

    @Transactional
    public void delete(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }

        // Check for associated AgentInstances
        long agentInstanceCount = AgentInstance.count("host.id", id);
        if (agentInstanceCount > 0) {
            throw new WebApplicationException(
                "Cannot delete host with " + agentInstanceCount + " associated agent instances",
                Response.Status.CONFLICT
            );
        }

        if (!Host.deleteById(id)) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
    }

    public HostInstallGuideRecord getInstallGuide(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }

        HostAgentResourceResolver.ResolvedHostAgentResource resource = resolveHostAgentResource(host);
        boolean windows = resource.osType() == OsType.WINDOWS;
        String packageFileName = getPackageFileName(resource.osType());
        String installCommand = windows ? "install.bat" : "./install.sh";
        String startCommand = windows ? "start.bat" : "./start.sh";
        String stopCommand = windows ? "stop.bat" : "./stop.sh";
        String uninstallCommand = windows ? "uninstall.bat" : "./uninstall.sh";
        String updateCommand = windows ? "update.bat <new-package-dir>" : "./update.sh <new-package-dir>";
        String logPath = windows ? ".\\logs\\host-agent.log" : "./logs/host-agent.log";
        String pidFile = windows ? ".\\host-agent.pid" : "./host-agent.pid";

        // Extract GitHub Releases URL from source config if available
        String githubReleaseUrl = extractGithubReleaseUrl(resource.sourceId());

        return new HostInstallGuideRecord(
                host.getId(),
                host.getSecretKey(),
                installCommand,
                "",
                null,
                packageFileName,
                startCommand,
                stopCommand,
                uninstallCommand,
                updateCommand,
                logPath,
                pidFile,
                new HostInstallGuideRecord.HostAgentResourceRecord(
                        resource.sourceId(),
                        resource.sourceName(),
                        resource.fileName(),
                        resource.osType().name()
                ),
                githubReleaseUrl
        );
    }

    private String extractGithubReleaseUrl(UUID sourceId) {
        try {
            String configJson = getAgentSourceConfig(sourceId);
            if (configJson == null || configJson.isBlank()) {
                return null;
            }
            JsonNode config = objectMapper.readTree(configJson);
            
            // Check for direct GitHub Releases URL configuration
            for (String key : List.of("githubReleaseUrl", "releaseUrl", "githubUrl")) {
                String value = config.path(key).asText();
                if (value != null && !value.isBlank() && value.contains("github.com")) {
                    return value;
                }
            }
            
            // Check if the URL field itself is a GitHub Releases URL
            String url = config.path("url").asText();
            if (url != null && !url.isBlank() && url.contains("github.com") && url.contains("/releases/")) {
                return url;
            }
        } catch (Exception e) {
            Log.warnf("Failed to extract GitHub Releases URL: %s", e.getMessage());
        }
        return null;
    }

    private String getAgentSourceConfig(UUID sourceId) {
        try {
            AgentSource source = AgentSource.findById(sourceId);
            return source != null ? source.getConfig() : null;
        } catch (Exception e) {
            Log.warnf("Failed to get agent source config: %s", e.getMessage());
            return null;
        }
    }

    public StreamingOutput downloadPackage(UUID id, UUID sourceId) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }

        HostAgentResourceResolver.ResolvedHostAgentResource resource = resolveHostAgentResource(host);
        if (!resource.sourceId().equals(sourceId)) {
            throw new WebApplicationException(
                    "sourceId does not match the HostAgent resource bound to host OS: " + resource.osType().name(),
                    Response.Status.BAD_REQUEST
            );
        }

        InputStream sourceStream = agentSourceService.getSourceStream(sourceId, new String[1]);
        String configContent = generateConfigFile(host);

        return output -> {
            try (sourceStream) {
                packageBuilder.writePackage(output, resource, sourceStream, configContent);
            } catch (Exception e) {
                Log.errorf("Failed to create package for host %s: %s", id, e.getMessage());
                throw new WebApplicationException("Failed to create package", e);
            }
        };
    }

    public String generateConfigFile(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        return generateConfigFile(host);
    }

    public String getPackageFileName(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        return getPackageFileName(resolveHostAgentResource(host).osType());
    }

    private String generateConfigFile(Host host) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("# HostAgent Configuration\n");
        yaml.append("listen_port: ").append(host.getListenPort() != null ? host.getListenPort() : 9090).append("\n");
        yaml.append("host_id: ").append(host.getId()).append("\n");
        yaml.append("secret_key: ").append(host.getSecretKey()).append("\n");
        yaml.append("heartbeat_interval: ").append(host.getHeartbeatInterval() != null ? host.getHeartbeatInterval() : 30).append("s\n");
        
        if (host.getConfig() != null && !host.getConfig().isBlank()) {
            yaml.append("\n# User defined config\n");
            yaml.append(host.getConfig());
        }
        return yaml.toString();
    }

    private HostAgentResourceResolver.ResolvedHostAgentResource resolveHostAgentResource(Host host) {
        List<AgentTemplate> templates = AgentTemplate.<AgentTemplate>listAll();
        List<HostAgentResourceResolver.HostAgentCandidate> candidates = templates.stream()
                .filter(template -> template.source != null)
                .map(template -> new HostAgentResourceResolver.HostAgentCandidate(
                        template.source.id,
                        template.source.name,
                        template.source.config,
                        template.osType
                ))
                .toList();
        return resourceResolver.resolve(host.getOs(), candidates);
    }

    private String getPackageFileName(OsType osType) {
        if (osType == OsType.WINDOWS) {
            return "host-agent-windows-amd64.zip";
        } else if (osType == OsType.MACOS) {
            return "host-agent-macos-arm64.tar.gz";
        } else {
            return "host-agent-linux-amd64.tar.gz";
        }
    }

    private HostRecord toDto(Host host) {
        return new HostRecord(
            host.id,
            host.identifier,
            host.name,
            host.hostname,
            host.os,
            host.cpuInfo,
            host.memInfo,
            host.ip,
            host.port,
            host.status,
            host.environment.id,
            host.environment.name,
            host.description,
            host.tags,
            host.enabled,
            host.createdAt,
            host.updatedAt,
            host.lastSeenAt,
            host.lastHeartbeat,
            host.config,
            host.heartbeatInterval,
            host.gatewayUrl,
            host.listenPort
        );
    }
}
