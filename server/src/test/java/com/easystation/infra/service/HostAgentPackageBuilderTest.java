package com.easystation.infra.service;

import com.easystation.agent.domain.enums.OsType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HostAgentPackageBuilderTest {

    private final HostAgentPackageBuilder builder = new HostAgentPackageBuilder();

    @Test
    void linuxPackageRunsInBackgroundAndPreservesConfigOnUpdate() throws IOException {
        Map<String, String> entries = buildPackage(
                new HostAgentResourceResolver.ResolvedHostAgentResource(
                        UUID.randomUUID(),
                        "Host Agent (Linux)",
                        "host-agent-linux-amd64",
                        OsType.LINUX
                )
        );

        assertTrue(entries.containsKey("host-agent-linux-amd64"));
        assertTrue(entries.containsKey("config.yaml"));
        assertTrue(entries.containsKey("install.sh"));
        assertTrue(entries.containsKey("start.sh"));
        assertTrue(entries.containsKey("stop.sh"));
        assertTrue(entries.containsKey("update.sh"));
        assertFalse(entries.containsKey("uninstall.sh"));

        String startScript = entries.get("start.sh");
        assertTrue(startScript.contains("nohup"));
        assertTrue(startScript.contains("host-agent.pid"));
        assertTrue(startScript.contains("host-agent.log"));

        String updateScript = entries.get("update.sh");
        assertTrue(updateScript.contains("config.yaml preserved"));
        assertFalse(updateScript.contains("config.yaml\" \"$SCRIPT_DIR/config.yaml"));
    }

    @Test
    void windowsPackageUsesBackgroundProcessAndLeavesPidAndLogs() throws IOException {
        Map<String, String> entries = buildPackage(
                new HostAgentResourceResolver.ResolvedHostAgentResource(
                        UUID.randomUUID(),
                        "Host Agent (Windows)",
                        "host-agent-windows-amd64.exe",
                        OsType.WINDOWS
                )
        );

        assertTrue(entries.containsKey("install.bat"));
        assertTrue(entries.containsKey("start.bat"));
        assertTrue(entries.containsKey("stop.bat"));
        assertTrue(entries.containsKey("update.bat"));
        assertFalse(entries.containsKey("uninstall.bat"));

        String startScript = entries.get("start.bat");
        assertTrue(startScript.contains("Start-Process"));
        assertTrue(startScript.contains("host-agent.pid"));
        assertTrue(startScript.contains("logs"));
    }

    private Map<String, String> buildPackage(HostAgentResourceResolver.ResolvedHostAgentResource resource) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bytes)) {
            builder.writePackage(
                    zos,
                    resource,
                    new ByteArrayInputStream("agent-binary".getBytes(StandardCharsets.UTF_8)),
                    "host_id: test-host\nsecret_key: secret\n"
            );
        }

        Map<String, String> entries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.put(entry.getName(), new String(zis.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }
}
