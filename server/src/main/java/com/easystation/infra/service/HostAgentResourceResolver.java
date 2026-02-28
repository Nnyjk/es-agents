package com.easystation.infra.service;

import com.easystation.agent.domain.enums.OsType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class HostAgentResourceResolver {

    @Inject
    ObjectMapper objectMapper;

    public ResolvedHostAgentResource resolve(String hostOs, List<HostAgentCandidate> candidates) {
        OsType targetOs = normalizeHostOs(hostOs);
        return candidates.stream()
                .filter(candidate -> candidate.sourceId() != null)
                .sorted(Comparator.comparingInt(candidate -> candidate.osType() == targetOs ? 0 : 1))
                .filter(candidate -> candidate.osType() == targetOs || candidate.osType() == OsType.ALL)
                .map(candidate -> toResolved(candidate, targetOs))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(
                        "No HostAgent resource matched host OS: " + targetOs.name(),
                        Response.Status.NOT_FOUND
                ));
    }

    OsType normalizeHostOs(String hostOs) {
        if (hostOs == null || hostOs.isBlank()) {
            throw new WebApplicationException("Host OS is required", Response.Status.BAD_REQUEST);
        }

        String normalized = hostOs.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "LINUX" -> OsType.LINUX;
            case "WINDOWS", "WIN" -> OsType.WINDOWS;
            case "LINUX_DOCKER", "DOCKER" -> OsType.LINUX_DOCKER;
            default -> throw new WebApplicationException(
                    "Unsupported host OS for HostAgent package: " + hostOs,
                    Response.Status.BAD_REQUEST
            );
        };
    }

    private ResolvedHostAgentResource toResolved(HostAgentCandidate candidate, OsType targetOs) {
        JsonNode config = readConfig(candidate.sourceConfig());
        String fileName = resolveFileName(config);
        if (fileName == null || fileName.isBlank()) {
            throw new WebApplicationException(
                    "HostAgent source is missing file metadata: " + candidate.sourceName(),
                    Response.Status.BAD_REQUEST
            );
        }

        String normalizedFileName = Path.of(fileName).getFileName().toString();
        return new ResolvedHostAgentResource(
                candidate.sourceId(),
                candidate.sourceName(),
                normalizedFileName,
                targetOs
        );
    }

    private JsonNode readConfig(String sourceConfig) {
        if (sourceConfig == null || sourceConfig.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(sourceConfig);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException("Failed to parse HostAgent source config", Response.Status.BAD_REQUEST);
        }
    }

    private String resolveFileName(JsonNode config) {
        List<String> directKeys = List.of("fileName", "file", "assetName", "artifactName");
        for (String key : directKeys) {
            String value = config.path(key).asText();
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        String nested = findNestedFileName(config.path("target"));
        if (nested != null) {
            return nested;
        }

        JsonNode targets = config.path("targets");
        if (targets.isArray()) {
            for (JsonNode target : targets) {
                String value = findNestedFileName(target);
                if (value != null) {
                    return value;
                }
            }
        }

        List<String> urlKeys = new ArrayList<>(List.of("url", "downloadUrl"));
        for (String key : urlKeys) {
            String value = config.path(key).asText();
            if (value != null && !value.isBlank()) {
                String fileName = fileNameFromUrl(value);
                if (fileName != null) {
                    return fileName;
                }
            }
        }

        String filePath = config.path("filePath").asText();
        if (filePath != null && !filePath.isBlank()) {
            return Path.of(filePath).getFileName().toString();
        }
        return null;
    }

    private String findNestedFileName(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        for (String key : List.of("fileName", "file", "assetName", "artifactName")) {
            String value = node.path(key).asText();
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        String url = node.path("url").asText();
        if (url != null && !url.isBlank()) {
            return fileNameFromUrl(url);
        }
        return null;
    }

    private String fileNameFromUrl(String rawUrl) {
        try {
            String path = URI.create(rawUrl).getPath();
            if (path == null || path.isBlank() || path.endsWith("/")) {
                return null;
            }
            return Path.of(path).getFileName().toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public record HostAgentCandidate(
            UUID sourceId,
            String sourceName,
            String sourceConfig,
            OsType osType
    ) {}

    public record ResolvedHostAgentResource(
            UUID sourceId,
            String sourceName,
            String fileName,
            OsType osType
    ) {}
}
