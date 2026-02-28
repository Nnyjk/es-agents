package com.easystation.infra.service;

import com.easystation.agent.domain.enums.OsType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HostAgentResourceResolverTest {

    private HostAgentResourceResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new HostAgentResourceResolver();
        resolver.objectMapper = new ObjectMapper();
    }

    @Test
    void resolvesResourceByHostOs() {
        UUID linuxSourceId = UUID.randomUUID();
        UUID windowsSourceId = UUID.randomUUID();

        HostAgentResourceResolver.ResolvedHostAgentResource resolved = resolver.resolve(
                "linux",
                List.of(
                        new HostAgentResourceResolver.HostAgentCandidate(
                                windowsSourceId,
                                "Host Agent (Windows)",
                                "{\"url\":\"https://example.com/releases/host-agent-windows-amd64.exe\"}",
                                OsType.WINDOWS
                        ),
                        new HostAgentResourceResolver.HostAgentCandidate(
                                linuxSourceId,
                                "Host Agent (Linux)",
                                "{\"fileName\":\"host-agent-linux-amd64\"}",
                                OsType.LINUX
                        )
                )
        );

        assertEquals(linuxSourceId, resolved.sourceId());
        assertEquals("Host Agent (Linux)", resolved.sourceName());
        assertEquals("host-agent-linux-amd64", resolved.fileName());
        assertEquals(OsType.LINUX, resolved.osType());
    }

    @Test
    void rejectsUnsupportedHostOs() {
        WebApplicationException error = assertThrows(
                WebApplicationException.class,
                () -> resolver.resolve("macos", List.of())
        );

        assertEquals(400, error.getResponse().getStatus());
    }

    @Test
    void returnsNotFoundWhenNoSourceMatchesHostOs() {
        WebApplicationException error = assertThrows(
                WebApplicationException.class,
                () -> resolver.resolve(
                        "windows",
                        List.of(new HostAgentResourceResolver.HostAgentCandidate(
                                UUID.randomUUID(),
                                "Linux Only",
                                "{\"file\":\"host-agent-linux-amd64\"}",
                                OsType.LINUX
                        ))
                )
        );

        assertEquals(404, error.getResponse().getStatus());
    }
}
