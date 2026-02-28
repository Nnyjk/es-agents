package com.easystation.infra.record;

import java.util.UUID;

public record HostInstallGuideRecord(
    UUID hostId,
    String secretKey,
    String installScript,
    String dockerCommand,
    String downloadUrl,
    String packageFileName,
    String startCommand,
    String stopCommand,
    String updateCommand,
    String logPath,
    String pidFile,
    HostAgentResourceRecord source
) {
    public record HostAgentResourceRecord(
        UUID sourceId,
        String sourceName,
        String fileName,
        String osType
    ) {}
}
