package com.easystation.auth.service;

import com.easystation.auth.domain.ApiKey;
import com.easystation.auth.domain.ApiKeyUsageLog;
import com.easystation.auth.dto.ApiKeyRecord;
import io.quarkus.logging.Log;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ApiKeyService {

    private static final String KEY_PREFIX = "esak_";
    private static final int SECRET_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    @ConfigProperty(name = "auth.jwt.issuer", defaultValue = "https://easystation.com/issuer")
    String issuer;

    @ConfigProperty(name = "api-key.encryption.key", defaultValue = "EasyStationApiKeySecretKey32")
    String encryptionKey;

    @ConfigProperty(name = "api-key.default.expires-days", defaultValue = "365")
    int defaultExpiresDays;

    public List<ApiKeyRecord.DetailWithoutKey> list(ApiKeyRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and name like :keyword");
            params.put("keyword", "%" + query.keyword() + "%");
        }
        if (query.createdBy() != null) {
            sql.append(" and createdBy = :createdBy");
            params.put("createdBy", query.createdBy());
        }
        if (query.enabled() != null) {
            sql.append(" and enabled = :enabled");
            params.put("enabled", query.enabled());
        }
        if (query.revoked() != null) {
            if (query.revoked()) {
                sql.append(" and revokedAt is not null");
            } else {
                sql.append(" and revokedAt is null");
            }
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        List<ApiKey> keys = ApiKey.<ApiKey>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .list();

        return keys.stream()
                .filter(k -> {
                    if (query.expired() == null) return true;
                    boolean expired = isExpired(k);
                    return query.expired() == expired;
                })
                .map(this::toDetailWithoutKey)
                .collect(Collectors.toList());
    }

    public ApiKeyRecord.DetailWithoutKey get(UUID id) {
        ApiKey key = ApiKey.findById(id);
        if (key == null) {
            throw new WebApplicationException("API Key not found", Response.Status.NOT_FOUND);
        }
        return toDetailWithoutKey(key);
    }

    public ApiKeyRecord.Detail getByKey(String keyValue) {
        String keyHash = hashKey(keyValue);
        ApiKey key = ApiKey.find("keyHash", keyHash).firstResult();
        if (key == null) {
            return null;
        }
        return toDetail(key, keyValue);
    }

    @Transactional
    public ApiKeyRecord.Detail create(ApiKeyRecord.Create dto) {
        // Generate secret
        String secret = generateSecret();
        String encryptedSecret = encryptSecret(secret);

        // Generate JWT token
        String key = generateJwtToken(dto.name(), dto.permissions(), dto.expiresAt());

        // Hash the key for storage
        String keyHash = hashKey(key);

        ApiKey apiKey = new ApiKey();
        apiKey.name = dto.name();
        apiKey.description = dto.description();
        apiKey.keyHash = keyHash;
        apiKey.secret = encryptedSecret;
        apiKey.expiresAt = dto.expiresAt() != null ? dto.expiresAt() : LocalDateTime.now().plusDays(defaultExpiresDays);
        apiKey.enabled = true;
        apiKey.permissions = dto.permissions() != null ? String.join(",", dto.permissions()) : "";
        apiKey.ipWhitelist = dto.ipWhitelist() != null ? String.join(",", dto.ipWhitelist()) : "";
        apiKey.createdBy = dto.createdBy();
        apiKey.persist();

        Log.infof("API Key created: %s", apiKey.name);
        return toDetail(apiKey, key);
    }

    @Transactional
    public ApiKeyRecord.DetailWithoutKey update(UUID id, ApiKeyRecord.Update dto) {
        ApiKey key = ApiKey.findById(id);
        if (key == null) {
            throw new WebApplicationException("API Key not found", Response.Status.NOT_FOUND);
        }

        if (key.revokedAt != null) {
            throw new WebApplicationException("Cannot update revoked API Key", Response.Status.BAD_REQUEST);
        }

        if (dto.name() != null) key.name = dto.name();
        if (dto.description() != null) key.description = dto.description();
        if (dto.expiresAt() != null) key.expiresAt = dto.expiresAt();
        if (dto.enabled() != null) key.enabled = dto.enabled();
        if (dto.permissions() != null) key.permissions = String.join(",", dto.permissions());
        if (dto.ipWhitelist() != null) key.ipWhitelist = String.join(",", dto.ipWhitelist());

        return toDetailWithoutKey(key);
    }

    @Transactional
    public ApiKeyRecord.DetailWithoutKey revoke(UUID id, ApiKeyRecord.RevokeRequest dto) {
        ApiKey key = ApiKey.findById(id);
        if (key == null) {
            throw new WebApplicationException("API Key not found", Response.Status.NOT_FOUND);
        }

        if (key.revokedAt != null) {
            throw new WebApplicationException("API Key already revoked", Response.Status.BAD_REQUEST);
        }

        key.revokedAt = LocalDateTime.now();
        key.revokedBy = dto.revokedBy();
        key.revokeReason = dto.reason();
        key.enabled = false;

        Log.infof("API Key revoked: %s", key.name);
        return toDetailWithoutKey(key);
    }

    @Transactional
    public ApiKeyRecord.Detail refresh(UUID id, ApiKeyRecord.RefreshRequest dto) {
        ApiKey key = ApiKey.findById(id);
        if (key == null) {
            throw new WebApplicationException("API Key not found", Response.Status.NOT_FOUND);
        }

        if (key.revokedAt != null) {
            throw new WebApplicationException("Cannot refresh revoked API Key", Response.Status.BAD_REQUEST);
        }

        // Generate new secret
        String secret = generateSecret();
        String encryptedSecret = encryptSecret(secret);

        // Generate new JWT token
        List<String> permissions = key.permissions != null && !key.permissions.isEmpty()
                ? Arrays.asList(key.permissions.split(","))
                : new ArrayList<>();
        String newKey = generateJwtToken(key.name, permissions, key.expiresAt);

        // Update key hash
        key.keyHash = hashKey(newKey);
        key.secret = encryptedSecret;

        Log.infof("API Key refreshed: %s", key.name);
        return toDetail(key, newKey);
    }

    @Transactional
    public void delete(UUID id) {
        ApiKey key = ApiKey.findById(id);
        if (key == null) {
            throw new WebApplicationException("API Key not found", Response.Status.NOT_FOUND);
        }

        // Delete associated usage logs
        ApiKeyUsageLog.delete("keyId", id);
        key.delete();

        Log.infof("API Key deleted: %s", key.name);
    }

    public ApiKeyRecord.ValidationResult validate(String keyValue, String clientIp, String requiredPermission) {
        String keyHash = hashKey(keyValue);
        ApiKey key = ApiKey.find("keyHash", keyHash).firstResult();

        if (key == null) {
            return new ApiKeyRecord.ValidationResult(false, null, null, null, "API Key not found");
        }

        if (key.revokedAt != null) {
            return new ApiKeyRecord.ValidationResult(false, key.id, key.name, null, "API Key has been revoked");
        }

        if (!key.enabled) {
            return new ApiKeyRecord.ValidationResult(false, key.id, key.name, null, "API Key is disabled");
        }

        if (isExpired(key)) {
            return new ApiKeyRecord.ValidationResult(false, key.id, key.name, null, "API Key has expired");
        }

        // Check IP whitelist
        if (key.ipWhitelist != null && !key.ipWhitelist.isEmpty() && clientIp != null) {
            List<String> allowedIps = Arrays.asList(key.ipWhitelist.split(","));
            if (!allowedIps.contains(clientIp)) {
                return new ApiKeyRecord.ValidationResult(false, key.id, key.name, null, "IP not in whitelist");
            }
        }

        // Check permissions
        List<String> permissions = key.permissions != null && !key.permissions.isEmpty()
                ? Arrays.asList(key.permissions.split(","))
                : new ArrayList<>();
        if (requiredPermission != null && !permissions.contains(requiredPermission) && !permissions.contains("*")) {
            return new ApiKeyRecord.ValidationResult(false, key.id, key.name, permissions, "Permission denied: " + requiredPermission);
        }

        return new ApiKeyRecord.ValidationResult(true, key.id, key.name, permissions, "API Key is valid");
    }

    @Transactional
    public void logUsage(UUID keyId, String clientIp, String method, String path,
                         Integer status, Long responseTime, String permission, String errorMessage) {
        ApiKeyUsageLog log = new ApiKeyUsageLog();
        log.keyId = keyId;
        log.usageTime = LocalDateTime.now();
        log.clientIp = clientIp;
        log.requestMethod = method;
        log.requestPath = path;
        log.responseStatus = status;
        log.responseTimeMs = responseTime;
        log.permissionUsed = permission;
        log.errorMessage = errorMessage;
        log.persist();
    }

    public List<ApiKeyRecord.UsageLogDetail> getUsageLogs(ApiKeyRecord.UsageLogQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.keyId() != null) {
            sql.append(" and keyId = :keyId");
            params.put("keyId", query.keyId());
        }
        if (query.clientIp() != null && !query.clientIp().isBlank()) {
            sql.append(" and clientIp = :clientIp");
            params.put("clientIp", query.clientIp());
        }
        if (query.requestMethod() != null && !query.requestMethod().isBlank()) {
            sql.append(" and requestMethod = :requestMethod");
            params.put("requestMethod", query.requestMethod());
        }
        if (query.requestPath() != null && !query.requestPath().isBlank()) {
            sql.append(" and requestPath like :requestPath");
            params.put("requestPath", "%" + query.requestPath() + "%");
        }
        if (query.responseStatus() != null) {
            sql.append(" and responseStatus = :responseStatus");
            params.put("responseStatus", query.responseStatus());
        }
        if (query.startTime() != null) {
            sql.append(" and usageTime >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and usageTime <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return ApiKeyUsageLog.<ApiKeyUsageLog>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toUsageLogDetail)
                .collect(Collectors.toList());
    }

    private String generateSecret() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        for (int i = 0; i < SECRET_LENGTH; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateJwtToken(String name, List<String> permissions, LocalDateTime expiresAt) {
        long expiresIn = expiresAt != null
                ? (expiresAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() - System.currentTimeMillis() / 1000)
                : defaultExpiresDays * 24 * 3600L;

        return Jwt.issuer(issuer)
                .claim("name", name)
                .claim("type", "api-key")
                .claim("permissions", permissions != null ? permissions : new ArrayList<>())
                .expiresIn(expiresIn)
                .sign();
    }

    private byte[] getAesKeyBytes() {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive AES key", e);
        }
    }

    private String encryptSecret(String secret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(getAesKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt secret", e);
        }
    }

    private String decryptSecret(String encryptedSecret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(getAesKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedSecret));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt secret", e);
        }
    }

    private String hashKey(String key) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }

    private boolean isExpired(ApiKey key) {
        return key.expiresAt != null && key.expiresAt.isBefore(LocalDateTime.now());
    }

    private List<String> parseList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(","));
    }

    private ApiKeyRecord.Detail toDetail(ApiKey key, String keyValue) {
        boolean expired = isExpired(key);
        boolean revoked = key.revokedAt != null;
        boolean valid = key.enabled && !revoked && !expired;

        return new ApiKeyRecord.Detail(
                key.id,
                keyValue,
                key.name,
                key.description,
                key.expiresAt,
                key.enabled,
                parseList(key.permissions),
                parseList(key.ipWhitelist),
                key.createdBy,
                key.createdAt,
                key.updatedAt,
                key.revokedAt,
                key.revokedBy,
                key.revokeReason,
                expired,
                valid,
                revoked
        );
    }

    private ApiKeyRecord.DetailWithoutKey toDetailWithoutKey(ApiKey key) {
        boolean expired = isExpired(key);
        boolean revoked = key.revokedAt != null;
        boolean valid = key.enabled && !revoked && !expired;

        // Get last usage time
        ApiKeyUsageLog lastLog = ApiKeyUsageLog.find("keyId = ?1 ORDER BY usageTime DESC LIMIT 1", key.id)
                .firstResult();
        LocalDateTime lastUsedAt = lastLog != null ? lastLog.usageTime : null;

        return new ApiKeyRecord.DetailWithoutKey(
                key.id,
                key.name,
                key.description,
                key.expiresAt,
                key.enabled,
                parseList(key.permissions),
                parseList(key.ipWhitelist),
                key.createdBy,
                key.createdAt,
                key.updatedAt,
                lastUsedAt,
                key.revokedAt,
                key.revokedBy,
                key.revokeReason,
                expired,
                valid,
                revoked
        );
    }

    private ApiKeyRecord.UsageLogDetail toUsageLogDetail(ApiKeyUsageLog log) {
        return new ApiKeyRecord.UsageLogDetail(
                log.id,
                log.keyId,
                log.usageTime,
                log.clientIp,
                log.requestMethod,
                log.requestPath,
                log.responseStatus,
                log.responseTimeMs,
                log.permissionUsed,
                log.errorMessage,
                log.createdAt
        );
    }
}