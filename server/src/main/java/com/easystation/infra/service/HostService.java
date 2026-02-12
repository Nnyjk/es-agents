package com.easystation.infra.service;

import com.easystation.agent.service.AgentSourceService;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class HostService {

    @Inject
    AgentSourceService agentSourceService;

    @Inject
    AgentConnectionManager connectionManager;

    public void connect(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        connectionManager.connect(host);
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
        
        int port = host.getListenPort() != null ? host.getListenPort() : 9090;
        String installScript = String.format("curl -sfL https://easystation.com/install.sh | sudo bash -s -- --host-id %s --secret %s --listen-port %d", host.getId(), host.getSecretKey(), port);
        String dockerCommand = String.format("docker run -d --name easy-agent -p %d:%d -e HOST_ID=%s -e AGENT_SECRET=%s -e LISTEN_PORT=%d easystation/agent:latest", port, port, host.getId(), host.getSecretKey(), port);
        String downloadUrl = "/api/infra/hosts/" + host.getId() + "/package";
        return new HostInstallGuideRecord(host.getId(), host.getSecretKey(), installScript, dockerCommand, downloadUrl);
    }

    public StreamingOutput downloadPackage(UUID id, UUID sourceId) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        
        // 1. Get Agent Binary Stream from Source Service
        String[] fileName = new String[1];
        InputStream sourceStream = agentSourceService.getSourceStream(sourceId, fileName);
        
        // 2. Determine scripts based on OS (simplified check based on filename or explicit parameter, here we guess by filename)
        boolean isWindows = fileName[0].toLowerCase().endsWith(".exe");
        
        return output -> {
            try (ZipOutputStream zos = new ZipOutputStream(output);
                 sourceStream) {
                
                // Add Agent Binary
                ZipEntry binaryEntry = new ZipEntry(fileName[0]);
                zos.putNextEntry(binaryEntry);
                sourceStream.transferTo(zos);
                zos.closeEntry();
                
                // Add Config
                ZipEntry configEntry = new ZipEntry("config.yaml");
                zos.putNextEntry(configEntry);
                String configContent = generateConfigFile(id);
                zos.write(configContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                
                // Add Scripts
                if (isWindows) {
                    addWindowsScripts(zos, fileName[0]);
                } else {
                    addLinuxScripts(zos, fileName[0]);
                }
                
            } catch (Exception e) {
                throw new WebApplicationException("Failed to create package", e);
            }
        };
    }

    private void addLinuxScripts(ZipOutputStream zos, String binaryName) throws IOException {
        // start.sh
        String startScript = "#!/bin/bash\n" +
                "chmod +x " + binaryName + "\n" +
                "nohup ./" + binaryName + " > agent.log 2>&1 & echo $! > pid\n" +
                "echo \"Agent started with PID $(cat pid)\"\n";
        zos.putNextEntry(new ZipEntry("start.sh"));
        zos.write(startScript.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        
        // stop.sh
        String stopScript = "#!/bin/bash\n" +
                "if [ -f pid ]; then\n" +
                "  kill $(cat pid)\n" +
                "  rm pid\n" +
                "  echo \"Agent stopped\"\n" +
                "else\n" +
                "  echo \"PID file not found\"\n" +
                "fi\n";
        zos.putNextEntry(new ZipEntry("stop.sh"));
        zos.write(stopScript.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void addWindowsScripts(ZipOutputStream zos, String binaryName) throws IOException {
        // start.bat
        String startScript = "@echo off\n" +
                "start /B " + binaryName + " > agent.log 2>&1\n" +
                "echo Agent started in background.\n";
        zos.putNextEntry(new ZipEntry("start.bat"));
        zos.write(startScript.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        
        // stop.bat (Simple kill by name, not ideal but works for basic usage)
        String stopScript = "@echo off\n" +
                "taskkill /F /IM " + binaryName + "\n" +
                "echo Agent stopped.\n";
        zos.putNextEntry(new ZipEntry("stop.bat"));
        zos.write(stopScript.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    public String generateConfigFile(UUID id) {
        Host host = Host.findById(id);
        if (host == null) {
            throw new WebApplicationException("Host not found", Response.Status.NOT_FOUND);
        }
        
        StringBuilder yaml = new StringBuilder();
        yaml.append("listen_port: ").append(host.getListenPort() != null ? host.getListenPort() : 9090).append("\n");
        yaml.append("host_id: ").append(host.getId()).append("\n");
        yaml.append("secret_key: ").append(host.getSecretKey()).append("\n");
        yaml.append("heartbeat_interval: ").append(host.getHeartbeatInterval() != null ? host.getHeartbeatInterval() : 30).append("\n");
        
        if (host.getConfig() != null && !host.getConfig().isBlank()) {
            yaml.append("\n# User defined config\n");
            yaml.append(host.getConfig());
        }
        return yaml.toString();
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
