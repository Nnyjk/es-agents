package com.easystation.agent.service;

import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.AgentSourceCache;
import com.easystation.agent.domain.AgentSourceVersion;
import com.easystation.agent.dto.AgentSourceVersionRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentSourceVersionService {

    private static final String CACHE_BASE_PATH = "/tmp/agent-cache";

    public List<AgentSourceVersionRecord.Detail> listVersions(AgentSourceVersionRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.sourceId() != null) {
            sql.append(" and sourceId = :sourceId");
            params.put("sourceId", query.sourceId());
        }
        if (query.version() != null && !query.version().isBlank()) {
            sql.append(" and version like :version");
            params.put("version", "%" + query.version() + "%");
        }
        if (query.verified() != null) {
            sql.append(" and verified = :verified");
            params.put("verified", query.verified());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return AgentSourceVersion.<AgentSourceVersion>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AgentSourceVersionRecord.Detail getVersion(UUID id) {
        AgentSourceVersion version = AgentSourceVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }
        return toDetail(version);
    }

    @Transactional
    public AgentSourceVersionRecord.Detail createVersion(AgentSourceVersionRecord.Create dto) {
        AgentSource source = AgentSource.findById(dto.sourceId());
        if (source == null) {
            throw new WebApplicationException("Source not found", Response.Status.NOT_FOUND);
        }

        // Check if version already exists
        if (AgentSourceVersion.count("sourceId = ?1 and version = ?2", dto.sourceId(), dto.version()) > 0) {
            throw new WebApplicationException("Version already exists for this source", Response.Status.CONFLICT);
        }

        AgentSourceVersion version = new AgentSourceVersion();
        version.sourceId = dto.sourceId();
        version.version = dto.version();
        version.filePath = dto.filePath();
        version.fileSize = dto.fileSize();
        version.checksumMd5 = dto.checksumMd5();
        version.checksumSha256 = dto.checksumSha256();
        version.description = dto.description();
        version.downloadUrl = dto.downloadUrl();
        version.verified = false;
        version.createdBy = dto.createdBy();
        version.persist();

        Log.infof("Version created: %s - %s", source.name, version.version);
        return toDetail(version);
    }

    @Transactional
    public AgentSourceVersionRecord.Detail updateVersion(UUID id, AgentSourceVersionRecord.Update dto) {
        AgentSourceVersion version = AgentSourceVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        if (dto.version() != null) version.version = dto.version();
        if (dto.filePath() != null) version.filePath = dto.filePath();
        if (dto.fileSize() != null) version.fileSize = dto.fileSize();
        if (dto.checksumMd5() != null) version.checksumMd5 = dto.checksumMd5();
        if (dto.checksumSha256() != null) version.checksumSha256 = dto.checksumSha256();
        if (dto.description() != null) version.description = dto.description();
        if (dto.downloadUrl() != null) version.downloadUrl = dto.downloadUrl();

        return toDetail(version);
    }

    @Transactional
    public void deleteVersion(UUID id) {
        AgentSourceVersion version = AgentSourceVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        // Delete associated cache
        AgentSourceCache.delete("versionId", id);
        version.delete();
        Log.infof("Version deleted: %s", version.version);
    }

    @Transactional
    public AgentSourceVersionRecord.Detail verifyVersion(UUID id, AgentSourceVersionRecord.VerifyRequest dto) {
        AgentSourceVersion version = AgentSourceVersion.findById(id);
        if (version == null) {
            throw new WebApplicationException("Version not found", Response.Status.NOT_FOUND);
        }

        version.verified = dto.verified();
        version.verifiedAt = LocalDateTime.now();
        version.verifiedBy = dto.verifiedBy();

        Log.infof("Version %s: %s", dto.verified() ? "verified" : "unverified", version.version);
        return toDetail(version);
    }

    public List<AgentSourceVersionRecord.CacheDetail> listCache(AgentSourceVersionRecord.CacheQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.sourceId() != null) {
            sql.append(" and sourceId = :sourceId");
            params.put("sourceId", query.sourceId());
        }
        if (query.valid() != null) {
            sql.append(" and valid = :valid");
            params.put("valid", query.valid());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return AgentSourceCache.<AgentSourceCache>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toCacheDetail)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentSourceVersionRecord.PullResult pull(AgentSourceVersionRecord.PullRequest dto) {
        AgentSource source = AgentSource.findById(dto.sourceId());
        if (source == null) {
            throw new WebApplicationException("Source not found", Response.Status.NOT_FOUND);
        }

        String versionStr = dto.version() != null ? dto.version() : "latest";
        String cacheKey = dto.sourceId() + "-" + versionStr;
        Path cachePath = Paths.get(CACHE_BASE_PATH, cacheKey);

        // Check cache
        if (dto.useCache()) {
            AgentSourceCache cache = AgentSourceCache.<AgentSourceCache>find("sourceId = ?1 and valid = ?2", 
                    dto.sourceId(), true).firstResult();
            if (cache != null && Files.exists(Paths.get(cache.cachePath))) {
                // Update last accessed time
                cache.lastAccessedAt = LocalDateTime.now();
                
                Log.infof("Using cached resource: %s", cache.cachePath);
                return new AgentSourceVersionRecord.PullResult(
                        dto.sourceId(),
                        versionStr,
                        cache.cachePath,
                        cache.cacheSize,
                        null,
                        true
                );
            }
        }

        // Pull from source (simplified - in real implementation would use AgentSourceService)
        try {
            Files.createDirectories(cachePath.getParent());
            
            // Create cache record
            AgentSourceCache cache = new AgentSourceCache();
            cache.sourceId = dto.sourceId();
            cache.cachePath = cachePath.toString();
            cache.valid = true;
            cache.lastAccessedAt = LocalDateTime.now();
            cache.persist();

            Log.infof("Resource pulled to cache: %s", cachePath);
            return new AgentSourceVersionRecord.PullResult(
                    dto.sourceId(),
                    versionStr,
                    cachePath.toString(),
                    cache.cacheSize,
                    null,
                    false
            );
        } catch (IOException e) {
            throw new WebApplicationException("Failed to pull resource: " + e.getMessage(), 
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void invalidateCache(UUID cacheId) {
        AgentSourceCache cache = AgentSourceCache.findById(cacheId);
        if (cache == null) {
            throw new WebApplicationException("Cache not found", Response.Status.NOT_FOUND);
        }

        cache.valid = false;
        
        // Optionally delete physical file
        try {
            Path path = Paths.get(cache.cachePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            Log.warnf("Failed to delete cache file: %s", e.getMessage());
        }

        Log.infof("Cache invalidated: %s", cache.cachePath);
    }

    @Transactional
    public void clearCache(UUID sourceId) {
        List<AgentSourceCache> caches = AgentSourceCache.<AgentSourceCache>find("sourceId", sourceId).list();
        for (AgentSourceCache cache : caches) {
            try {
                Path path = Paths.get(cache.cachePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch (IOException e) {
                Log.warnf("Failed to delete cache file: %s", e.getMessage());
            }
            cache.delete();
        }
        Log.infof("Cache cleared for source: %s", sourceId);
    }

    public String calculateChecksum(Path path, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] fileBytes = Files.readAllBytes(path);
            byte[] hash = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private AgentSourceVersionRecord.Detail toDetail(AgentSourceVersion version) {
        return new AgentSourceVersionRecord.Detail(
                version.id,
                version.sourceId,
                version.version,
                version.filePath,
                version.fileSize,
                version.checksumMd5,
                version.checksumSha256,
                version.description,
                version.verified,
                version.verifiedAt,
                version.verifiedBy,
                version.downloadUrl,
                version.createdBy,
                version.createdAt,
                version.updatedAt
        );
    }

    private AgentSourceVersionRecord.CacheDetail toCacheDetail(AgentSourceCache cache) {
        return new AgentSourceVersionRecord.CacheDetail(
                cache.id,
                cache.sourceId,
                cache.versionId,
                cache.cachePath,
                cache.cacheSize,
                cache.valid,
                cache.lastAccessedAt,
                cache.expiresAt,
                cache.createdAt
        );
    }
}