package com.easystation.auth.service;

import com.easystation.auth.domain.ApiToken;
import com.easystation.auth.domain.ApiTokenAccessLog;
import com.easystation.auth.domain.enums.TokenScope;
import com.easystation.auth.dto.ApiTokenRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ApiTokenService {

    private static final String TOKEN_PREFIX = "esa_";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    public List<ApiTokenRecord.Detail> list(ApiTokenRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and name like :keyword");
            params.put("keyword", "%" + query.keyword() + "%");
        }
        if (query.userId() != null) {
            sql.append(" and userId = :userId");
            params.put("userId", query.userId());
        }
        if (query.scope() != null) {
            sql.append(" and scope = :scope");
            params.put("scope", query.scope());
        }
        if (query.revoked() != null) {
            sql.append(" and revoked = :revoked");
            params.put("revoked", query.revoked());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        List<ApiToken> tokens = ApiToken.<ApiToken>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .list();

        return tokens.stream()
                .map(t -> toDetail(t, query.expired()))
                .collect(Collectors.toList());
    }

    public ApiTokenRecord.Detail get(UUID id) {
        ApiToken token = ApiToken.findById(id);
        if (token == null) {
            throw new WebApplicationException("Token not found", Response.Status.NOT_FOUND);
        }
        return toDetail(token, null);
    }

    public ApiTokenRecord.Detail getByToken(String tokenValue) {
        ApiToken token = ApiToken.find("token", tokenValue).firstResult();
        if (token == null) {
            return null;
        }
        return toDetail(token, null);
    }

    @Transactional
    public ApiTokenRecord.Detail create(ApiTokenRecord.Create dto) {
        ApiToken token = new ApiToken();
        token.token = generateToken();
        token.name = dto.name();
        token.userId = dto.userId();
        token.description = dto.description();
        token.scope = dto.scope() != null ? dto.scope() : TokenScope.READ_ONLY;
        token.expiresAt = dto.expiresAt();
        token.revoked = false;
        token.createdBy = dto.createdBy();
        token.persist();

        Log.infof("API Token created: %s", token.name);
        return toDetail(token, null);
    }

    @Transactional
    public ApiTokenRecord.Detail update(UUID id, ApiTokenRecord.Update dto) {
        ApiToken token = ApiToken.findById(id);
        if (token == null) {
            throw new WebApplicationException("Token not found", Response.Status.NOT_FOUND);
        }

        if (token.revoked) {
            throw new WebApplicationException("Cannot update revoked token", Response.Status.BAD_REQUEST);
        }

        if (dto.name() != null) token.name = dto.name();
        if (dto.description() != null) token.description = dto.description();
        if (dto.scope() != null) token.scope = dto.scope();
        if (dto.expiresAt() != null) token.expiresAt = dto.expiresAt();

        return toDetail(token, null);
    }

    @Transactional
    public ApiTokenRecord.Detail revoke(UUID id, ApiTokenRecord.RevokeRequest dto) {
        ApiToken token = ApiToken.findById(id);
        if (token == null) {
            throw new WebApplicationException("Token not found", Response.Status.NOT_FOUND);
        }

        if (token.revoked) {
            throw new WebApplicationException("Token already revoked", Response.Status.BAD_REQUEST);
        }

        token.revoked = true;
        token.revokedAt = LocalDateTime.now();
        token.revokedBy = dto.revokedBy();
        token.revokedReason = dto.reason();

        Log.infof("API Token revoked: %s", token.name);
        return toDetail(token, null);
    }

    @Transactional
    public void delete(UUID id) {
        ApiToken token = ApiToken.findById(id);
        if (token == null) {
            throw new WebApplicationException("Token not found", Response.Status.NOT_FOUND);
        }

        // Delete associated access logs
        ApiTokenAccessLog.delete("tokenId", id);
        token.delete();

        Log.infof("API Token deleted: %s", token.name);
    }

    public ApiTokenRecord.TokenValidation validate(String tokenValue) {
        ApiToken token = ApiToken.find("token", tokenValue).firstResult();

        if (token == null) {
            return new ApiTokenRecord.TokenValidation(false, null, null, null, "Token not found");
        }

        if (token.revoked) {
            return new ApiTokenRecord.TokenValidation(false, token.id, token.name, token.scope, "Token has been revoked");
        }

        if (token.expiresAt != null && token.expiresAt.isBefore(LocalDateTime.now())) {
            return new ApiTokenRecord.TokenValidation(false, token.id, token.name, token.scope, "Token has expired");
        }

        // Update last used time
        token.lastUsedAt = LocalDateTime.now();

        return new ApiTokenRecord.TokenValidation(true, token.id, token.name, token.scope, "Token is valid");
    }

    @Transactional
    public void logAccess(UUID tokenId, String clientIp, String method, String path, 
                          Integer status, Long responseTime, String requestBody, String errorMessage) {
        ApiTokenAccessLog log = new ApiTokenAccessLog();
        log.tokenId = tokenId;
        log.accessTime = LocalDateTime.now();
        log.clientIp = clientIp;
        log.requestMethod = method;
        log.requestPath = path;
        log.responseStatus = status;
        log.responseTimeMs = responseTime;
        log.requestBody = requestBody;
        log.errorMessage = errorMessage;
        log.persist();
    }

    public List<ApiTokenRecord.AccessLogDetail> getAccessLogs(ApiTokenRecord.AccessLogQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.tokenId() != null) {
            sql.append(" and tokenId = :tokenId");
            params.put("tokenId", query.tokenId());
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
            sql.append(" and accessTime >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and accessTime <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return ApiTokenAccessLog.<ApiTokenAccessLog>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toAccessLogDetail)
                .collect(Collectors.toList());
    }

    private String generateToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(TOKEN_PREFIX);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private boolean isExpired(ApiToken token) {
        return token.expiresAt != null && token.expiresAt.isBefore(LocalDateTime.now());
    }

    private ApiTokenRecord.Detail toDetail(ApiToken token, Boolean filterExpired) {
        boolean expired = isExpired(token);
        boolean valid = !token.revoked && !expired;

        // If filtering by expired status
        if (filterExpired != null && filterExpired != expired) {
            return null;
        }

        return new ApiTokenRecord.Detail(
                token.id,
                token.token,
                token.name,
                token.userId,
                token.description,
                token.scope,
                token.expiresAt,
                token.lastUsedAt,
                token.revoked,
                token.revokedAt,
                token.revokedBy,
                token.revokedReason,
                token.createdBy,
                token.createdAt,
                token.updatedAt,
                expired,
                valid
        );
    }

    private ApiTokenRecord.AccessLogDetail toAccessLogDetail(ApiTokenAccessLog log) {
        return new ApiTokenRecord.AccessLogDetail(
                log.id,
                log.tokenId,
                log.accessTime,
                log.clientIp,
                log.requestMethod,
                log.requestPath,
                log.responseStatus,
                log.responseTimeMs,
                log.errorMessage,
                log.createdAt
        );
    }
}