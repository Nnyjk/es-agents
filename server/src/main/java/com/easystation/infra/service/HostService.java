package com.easystation.infra.service;

import com.easystation.agent.service.AgentSourceService;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.infra.domain.Environment;
import com.easystation.infra.domain.Host;
import com.easystation.infra.domain.enums.HostStatus;
import com.easystation.infra.record.HostRecord;
import com.easystation.infra.record.HostInstallGuideRecord;
import com.easystation.infra.socket.AgentConnectionManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import io.quarkus.logging.Log;

import java.io.InputStream;
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
        
        Host host = new Host();
        host.setName(dto.name());
        host.setHostname(dto.hostname());
        host.setOs(dto.os());
        host.setEnvironment(env);
        host.setDescription(dto.description());
        host.setStatus(HostStatus.UNCONNECTED);
        host.setSecretKey(UUID.randomUUID().toString()); // Generate secret key
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
        
        if (dto.name() != null) host.setName(dto.name());
        if (dto.hostname() != null) host.setHostname(dto.hostname());
        if (dto.os() != null) host.setOs(dto.os());
        if (dto.description() != null) host.setDescription(dto.description());
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
        // Use GitHub releases URL for pre-built packages to avoid 502 errors from on-the-fly package building
        String downloadUrl = buildGitHubReleaseDownloadUrl(resource.osType());
        String packageFileName = getPackageFileName(resource.osType());
        String installCommand = windows ? "install.bat" : "./install.sh";
        String startCommand = windows ? "start.bat" : "./start.sh";
        String stopCommand = windows ? "stop.bat" : "./stop.sh";
        String updateCommand = windows ? "update.bat <new-package-dir>" : "./update.sh <new-package-dir>";
        String logPath = windows ? ".\\logs\\host-agent.log" : "./logs/host-agent.log";
        String pidFile = windows ? ".\\host-agent.pid" : "./host-agent.pid";

        return new HostInstallGuideRecord(
                host.getId(),
                host.getSecretKey(),
                installCommand,
                "",
                downloadUrl,
                packageFileName,
                startCommand,
                stopCommand,
                updateCommand,
                logPath,
                pidFile,
                new HostInstallGuideRecord.HostAgentResourceRecord(
                        resource.sourceId(),
                        resource.sourceName(),
                        resource.fileName(),
                        resource.osType().name()
                )
        );
    }

    /**
     * Build GitHub releases download URL for host agent package.
     * Uses 'latest' tag to always get the most recent stable release.
     */
    private String buildGitHubReleaseDownloadUrl(OsType osType) {
        String fileName = getPackageFileName(osType);
        return "https://github.com/Nnyjk/es-agents/releases/latest/download/" + fileName;
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
            return "host-agent-windows.zip";
        } else if (osType == OsType.MACOS) {
            return "host-agent-macos.tar.gz";
        } else {
            // LINUX and LINUX_DOCKER
            return "host-agent-linux.tar.gz";
        }
    }

    private HostRecord toDto(Host host) {
        return new HostRecord(
            host.id,
            host.name,
            host.hostname,
            host.os,
            host.cpuInfo,
            host.memInfo,
            host.status,
            host.environment.id,
            host.environment.name,
            host.description,
            host.createdAt,
            host.lastHeartbeat,
            host.config,
            host.heartbeatInterval,
            host.gatewayUrl,
            host.listenPort
        );
    }
}
