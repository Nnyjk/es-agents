package com.easystation.infra.record;

import java.util.UUID;

public record HostInstallGuideRecord(
    UUID hostId,
    String secretKey,
    String installScript,
    String dockerCommand,
    String downloadUrl
) {}
