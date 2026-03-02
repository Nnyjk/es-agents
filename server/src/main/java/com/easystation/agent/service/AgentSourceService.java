package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCredential;
import com.easystation.agent.domain.AgentRepository;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.enums.AgentSourceType;
import com.easystation.agent.record.AgentCredentialRecord;
import com.easystation.agent.record.AgentRepositoryRecord;
import com.easystation.agent.record.AgentSourceRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentSourceService {

    @Inject
    ObjectMapper objectMapper;

    public List<AgentSourceRecord> list() {
        return AgentSource.listAll().stream()
            .map(e -> (AgentSource) e)
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public AgentSourceRecord get(UUID id) {
        AgentSource source = AgentSource.findById(id);
        if (source == null) {
            throw new WebApplicationException("Agent Source not found", Response.Status.NOT_FOUND);
        }
        return toDto(source);
    }

    @Transactional
    public AgentSourceRecord create(AgentSourceRecord.Create dto) {
        AgentSource source = new AgentSource();
        source.setName(dto.name());
        source.setType(dto.type());
        source.setConfig(dto.config());
        source.setRepository(resolveRepository(dto.repositoryId()));
        source.setCredential(resolveCredential(dto.credentialId()));
        source.persist();
        return toDto(source);
    }
    
    @Transactional
    public AgentSourceRecord update(UUID id, AgentSourceRecord.Update dto) {
        AgentSource source = AgentSource.findById(id);
        if (source == null) {
            throw new WebApplicationException("Agent Source not found", Response.Status.NOT_FOUND);
        }
        
        if (dto.name() != null) source.setName(dto.name());
        if (dto.type() != null) source.setType(dto.type());
        if (dto.config() != null) source.setConfig(dto.config());
        if (dto.repositoryId() != null) source.setRepository(resolveRepository(dto.repositoryId()));
        if (dto.credentialId() != null) source.setCredential(resolveCredential(dto.credentialId()));
        
        return toDto(source);
    }

    @Transactional
    public void delete(UUID id) {
        if (!AgentSource.deleteById(id)) {
            throw new WebApplicationException("Agent Source not found", Response.Status.NOT_FOUND);
        }
    }

    public InputStream getSourceStream(UUID id, String[] fileNameOut) {
        AgentSource source = AgentSource.findById(id);
        if (source == null) {
            throw new WebApplicationException("Agent Source not found", Response.Status.NOT_FOUND);
        }

        try {
            JsonNode config = readConfig(source.getConfig());

            if (source.getType() == AgentSourceType.HTTP || source.getType() == AgentSourceType.HTTPS) {
                String url = config.path("url").asText();
                if (url == null || url.isEmpty()) {
                    throw new WebApplicationException("URL not configured for this source", Response.Status.INTERNAL_SERVER_ERROR);
                }

                String fileName = config.path("fileName").asText();
                if (fileName == null || fileName.isEmpty()) {
                    fileName = resolveFileName(url, "agent-package");
                }
                if (fileNameOut != null && fileNameOut.length > 0) {
                    fileNameOut[0] = fileName;
                }

                AgentCredential credential = source.getCredential();
                return downloadWithCredential(url, credential, source.getType());
            }

            if (source.getType() == AgentSourceType.GITLAB || source.getType() == AgentSourceType.MAVEN || source.getType() == AgentSourceType.NEXTCLOUD) {
                AgentRepository repository = source.getRepository();
                if (repository == null) {
                    throw new WebApplicationException("Repository not configured for this source", Response.Status.INTERNAL_SERVER_ERROR);
                }
                AgentCredential credential = source.getCredential() != null ? source.getCredential() : repository.getCredential();
                String url = buildRepositoryDownloadUrl(source.getType(), repository, config);
                String fileName = resolveRepositoryFileName(source.getType(), config, url);
                if (fileNameOut != null && fileNameOut.length > 0) {
                    fileNameOut[0] = fileName;
                }
                return downloadWithCredential(url, credential, source.getType());
            }

            if (source.getType() == AgentSourceType.LOCAL) {
                String fileName = config.path("file").asText();
                if (fileName == null || fileName.isEmpty()) {
                    throw new WebApplicationException("File not configured for this source", Response.Status.INTERNAL_SERVER_ERROR);
                }

                if (fileNameOut != null && fileNameOut.length > 0) {
                    fileNameOut[0] = fileName;
                }

                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("agents/" + fileName);
                if (is == null) {
                    throw new WebApplicationException("Agent binary not found in resources: " + fileName, Response.Status.NOT_FOUND);
                }
                return is;
            }

            throw new WebApplicationException("Download not supported for this source type", Response.Status.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException("Failed to parse source config", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private AgentSourceRecord toDto(AgentSource source) {
        return new AgentSourceRecord(
            source.id,
            source.name,
            source.type,
            source.config,
            toRepositorySimpleDto(source.repository),
            toCredentialSimpleDto(source.credential),
            source.createdAt,
            source.updatedAt
        );
    }

    private AgentRepository resolveRepository(UUID id) {
        if (id == null) {
            return null;
        }
        AgentRepository repository = AgentRepository.findById(id);
        if (repository == null) {
            throw new WebApplicationException("Repository not found", Response.Status.BAD_REQUEST);
        }
        return repository;
    }

    private AgentCredential resolveCredential(UUID id) {
        if (id == null) {
            return null;
        }
        AgentCredential credential = AgentCredential.findById(id);
        if (credential == null) {
            throw new WebApplicationException("Credential not found", Response.Status.BAD_REQUEST);
        }
        return credential;
    }

    private AgentRepositoryRecord.Simple toRepositorySimpleDto(AgentRepository repository) {
        if (repository == null) {
            return null;
        }
        return new AgentRepositoryRecord.Simple(
            repository.id,
            repository.name,
            repository.type
        );
    }

    private AgentCredentialRecord.Simple toCredentialSimpleDto(AgentCredential credential) {
        if (credential == null) {
            return null;
        }
        return new AgentCredentialRecord.Simple(
            credential.id,
            credential.name,
            credential.type
        );
    }

    private JsonNode readConfig(String config) throws JsonProcessingException {
        if (config == null || config.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(config);
    }

    private String buildRepositoryDownloadUrl(AgentSourceType type, AgentRepository repository, JsonNode config) {
        if (type == AgentSourceType.GITLAB) {
            String ref = config.path("ref").asText();
            if (ref == null || ref.isBlank()) {
                ref = repository.getDefaultBranch() != null ? repository.getDefaultBranch() : "main";
            }
            String filePath = config.path("filePath").asText();
            if (filePath == null || filePath.isBlank()) {
                throw new WebApplicationException("filePath not configured for GitLab source", Response.Status.INTERNAL_SERVER_ERROR);
            }
            String encodedProject = urlEncode(repository.getProjectPath());
            String encodedFile = urlEncode(filePath);
            String encodedRef = urlEncode(ref);
            return normalizeBaseUrl(repository.getBaseUrl()) + "/api/v4/projects/" + encodedProject + "/repository/files/" + encodedFile + "/raw?ref=" + encodedRef;
        }

        if (type == AgentSourceType.MAVEN) {
            String downloadUrl = config.path("downloadUrl").asText();
            if (downloadUrl != null && !downloadUrl.isBlank()) {
                return downloadUrl;
            }
            String groupId = config.path("groupId").asText();
            String artifactId = config.path("artifactId").asText();
            String version = config.path("version").asText();
            String packaging = config.path("packaging").asText("jar");
            String classifier = config.path("classifier").asText();
            if (groupId == null || groupId.isBlank() || artifactId == null || artifactId.isBlank() || version == null || version.isBlank()) {
                throw new WebApplicationException("Maven coordinates not configured", Response.Status.INTERNAL_SERVER_ERROR);
            }
            String basePath = normalizeBaseUrl(repository.getBaseUrl());
            if (repository.getProjectPath() != null && !repository.getProjectPath().isBlank()) {
                basePath = basePath + "/" + trimSlashes(repository.getProjectPath());
            }
            String groupPath = groupId.replace(".", "/");
            String fileName = buildMavenFileName(artifactId, version, classifier, packaging);
            return basePath + "/" + groupPath + "/" + artifactId + "/" + version + "/" + fileName;
        }

        if (type == AgentSourceType.NEXTCLOUD) {
            String downloadUrl = config.path("downloadUrl").asText();
            if (downloadUrl != null && !downloadUrl.isBlank()) {
                return downloadUrl;
            }
            String filePath = config.path("filePath").asText();
            if (filePath == null || filePath.isBlank()) {
                throw new WebApplicationException("filePath not configured for Nextcloud source", Response.Status.INTERNAL_SERVER_ERROR);
            }
            String basePath = normalizeBaseUrl(repository.getBaseUrl());
            if (repository.getProjectPath() != null && !repository.getProjectPath().isBlank()) {
                basePath = basePath + "/" + trimSlashes(repository.getProjectPath());
            }
            return basePath + "/" + trimSlashes(filePath);
        }

        throw new WebApplicationException("Repository download not supported for this type", Response.Status.BAD_REQUEST);
    }

    private String resolveRepositoryFileName(AgentSourceType type, JsonNode config, String url) {
        String fileName = config.path("fileName").asText();
        if (fileName != null && !fileName.isBlank()) {
            return fileName;
        }
        if (type == AgentSourceType.MAVEN) {
            String artifactId = config.path("artifactId").asText();
            String version = config.path("version").asText();
            String packaging = config.path("packaging").asText("jar");
            String classifier = config.path("classifier").asText();
            if (artifactId != null && !artifactId.isBlank() && version != null && !version.isBlank()) {
                return buildMavenFileName(artifactId, version, classifier, packaging);
            }
        }
        return resolveFileName(url, "agent-package");
    }

    private String buildMavenFileName(String artifactId, String version, String classifier, String packaging) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(artifactId).append("-").append(version);
        if (classifier != null && !classifier.isBlank()) {
            fileName.append("-").append(classifier);
        }
        fileName.append(".").append(packaging);
        return fileName.toString();
    }

    private String resolveFileName(String url, String defaultName) {
        int lastIndex = url.lastIndexOf('/');
        if (lastIndex >= 0 && lastIndex < url.length() - 1) {
            return url.substring(lastIndex + 1);
        }
        return defaultName;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String trimSlashes(String value) {
        if (value == null) {
            return "";
        }
        String result = value;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private InputStream downloadWithCredential(String url, AgentCredential credential, AgentSourceType sourceType) {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET();

        if (isGitHubUrl(url)) {
            builder.header("User-Agent", "easy-station-agent/1.0");
        }

        Map<String, String> headers = resolveAuthHeaders(credential, sourceType);
        headers.forEach(builder::header);

        try {
            HttpResponse<InputStream> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            String upstreamError = readResponseBody(response.body());
            String message = "Failed to download from URL. Upstream status: " + response.statusCode();
            if (!upstreamError.isBlank()) {
                message += ", details: " + upstreamError;
            }
            throw new WebApplicationException(message, Response.Status.BAD_GATEWAY);
        } catch (IOException e) {
            throw new WebApplicationException("Failed to download from URL. Upstream IO error: " + e.getMessage(), Response.Status.BAD_GATEWAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Download interrupted", Response.Status.BAD_GATEWAY);
        }
    }

    private boolean isGitHubUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) {
                return false;
            }
            return "github.com".equalsIgnoreCase(host) || "api.github.com".equalsIgnoreCase(host) || host.endsWith(".github.com");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String readResponseBody(InputStream body) {
        if (body == null) {
            return "";
        }
        try (InputStream input = body; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            String responseBody = output.toString(StandardCharsets.UTF_8);
            responseBody = responseBody.replaceAll("\\s+", " ").trim();
            if (responseBody.length() > 500) {
                return responseBody.substring(0, 500) + "...";
            }
            return responseBody;
        } catch (IOException e) {
            return "failed to read upstream error body: " + e.getMessage();
        }
    }

    private Map<String, String> resolveAuthHeaders(AgentCredential credential, AgentSourceType sourceType) {
        if (credential == null) {
            return Map.of();
        }
        JsonNode config;
        try {
            config = readConfig(credential.getConfig());
        } catch (JsonProcessingException e) {
            throw new WebApplicationException("Failed to parse credential config", Response.Status.INTERNAL_SERVER_ERROR);
        }
        String token = resolveCredentialToken(credential, config);
        if (token == null || token.isBlank()) {
            return Map.of();
        }
        String headerName = config.path("headerName").asText();
        String headerPrefix = config.path("headerPrefix").asText();
        if (headerName == null || headerName.isBlank()) {
            if (sourceType == AgentSourceType.GITLAB) {
                headerName = "PRIVATE-TOKEN";
            } else {
                headerName = "Authorization";
            }
        }
        if (headerPrefix == null || headerPrefix.isBlank()) {
            headerPrefix = sourceType == AgentSourceType.GITLAB ? "" : "Bearer ";
        }
        return Map.of(headerName, headerPrefix + token);
    }

    private String resolveCredentialToken(AgentCredential credential, JsonNode config) {
        switch (credential.getType()) {
            case STATIC_TOKEN:
                return config.path("token").asText();
            case API_TOKEN:
                return requestApiToken(config);
            case SCRIPT_TOKEN:
                return requestScriptToken(config);
            case SSO_TOKEN:
                String token = config.path("token").asText();
                if (token == null || token.isBlank()) {
                    token = config.path("accessToken").asText();
                }
                return token;
            default:
                return null;
        }
    }

    private String requestApiToken(JsonNode config) {
        String tokenUrl = config.path("tokenUrl").asText();
        if (tokenUrl == null || tokenUrl.isBlank()) {
            return null;
        }
        String clientId = config.path("clientId").asText();
        String clientSecret = config.path("clientSecret").asText();
        String scope = config.path("scope").asText();
        StringBuilder body = new StringBuilder();
        body.append("grant_type=client_credentials");
        if (clientId != null && !clientId.isBlank()) {
            body.append("&client_id=").append(urlEncode(clientId));
        }
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.append("&client_secret=").append(urlEncode(clientSecret));
        }
        if (scope != null && !scope.isBlank()) {
            body.append("&scope=").append(urlEncode(scope));
        }

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new WebApplicationException("Failed to request token: " + response.statusCode(), Response.Status.BAD_GATEWAY);
            }
            JsonNode responseJson = objectMapper.readTree(response.body());
            String accessToken = responseJson.path("access_token").asText();
            if (accessToken == null || accessToken.isBlank()) {
                accessToken = responseJson.path("token").asText();
            }
            return accessToken;
        } catch (IOException e) {
            throw new WebApplicationException("Failed to request token: " + e.getMessage(), Response.Status.BAD_GATEWAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Token request interrupted", Response.Status.BAD_GATEWAY);
        }
    }

    private String requestScriptToken(JsonNode config) {
        String baseToken = config.path("baseToken").asText();
        String script = config.path("script").asText();
        if (script == null || script.isBlank()) {
            return baseToken;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(buildScriptCommand(script));
            if (baseToken != null && !baseToken.isBlank()) {
                builder.environment().put("BASE_TOKEN", baseToken);
            }
            Process process = builder.start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            process.getInputStream().transferTo(output);
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            process.getErrorStream().transferTo(error);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new WebApplicationException("Script token generation failed", Response.Status.BAD_GATEWAY);
            }
            String token = output.toString(StandardCharsets.UTF_8).trim();
            return token.isBlank() ? baseToken : token;
        } catch (IOException e) {
            throw new WebApplicationException("Script token generation failed: " + e.getMessage(), Response.Status.BAD_GATEWAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Script token generation interrupted", Response.Status.BAD_GATEWAY);
        }
    }

    private List<String> buildScriptCommand(String script) {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("win")) {
            return List.of("cmd", "/c", script);
        }
        return List.of("bash", "-lc", script);
    }
}
