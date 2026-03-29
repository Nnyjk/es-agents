package com.easystation.auth.service;

import com.easystation.auth.domain.RefreshToken;
import com.easystation.system.domain.User;
import io.quarkus.logging.Log;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class TokenService {

    @Inject
    TokenBlacklistService tokenBlacklistService;

    @ConfigProperty(name = "auth.jwt.access-token.expires-in", defaultValue = "3600")
    long accessTokenExpiresIn;  // 默认 1 小时

    @ConfigProperty(name = "auth.jwt.refresh-token.expires-in", defaultValue = "604800")
    long refreshTokenExpiresIn;  // 默认 7 天

    @ConfigProperty(name = "auth.jwt.issuer", defaultValue = "https://easystation.com/issuer")
    String issuer;

    private static final long MFA_TOKEN_EXPIRY_SECONDS = 300; // 5 minutes

    /**
     * 生成访问令牌
     */
    public String generateToken(String username, Set<String> roles) {
        return Jwt.issuer(issuer)
                .upn(username)
                .groups(roles)
                .expiresIn(accessTokenExpiresIn)
                .sign();
    }

    /**
     * 生成访问令牌（带记住登录）
     */
    public String generateToken(String username, Set<String> roles, boolean rememberMe) {
        long expiresIn = rememberMe ? accessTokenExpiresIn * 24 : accessTokenExpiresIn;  // 记住登录：24 小时
        return Jwt.issuer(issuer)
                .upn(username)
                .groups(roles)
                .expiresIn(expiresIn)
                .claim("remember_me", rememberMe)
                .sign();
    }

    /**
     * 生成刷新令牌
     */
    @Transactional
    public String generateRefreshToken(User user, String deviceInfo, String ipAddress, String userAgent) {
        // 生成随机令牌
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // 计算令牌哈希
        String tokenHash = hashToken(token);

        // 创建刷新令牌记录
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.user = user;
        refreshToken.tokenHash = tokenHash;
        refreshToken.deviceInfo = deviceInfo;
        refreshToken.ipAddress = ipAddress;
        refreshToken.userAgent = userAgent;
        refreshToken.expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiresIn);
        refreshToken.persist();

        Log.infof("Created refresh token for user: %s", user.username);
        return token;
    }

    /**
     * 生成 MFA 令牌
     */
    public String generateMfaToken(UUID userId) {
        return Jwt.issuer(issuer)
                .upn(userId.toString())
                .claim("type", "mfa")
                .expiresIn(MFA_TOKEN_EXPIRY_SECONDS)
                .sign();
    }

    /**
     * 验证刷新令牌，返回用户ID
     */
    @Transactional
    public String validateRefreshToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = RefreshToken.findByTokenHash(tokenHash);

        if (stored == null || !stored.isValid()) {
            Log.warnf("Invalid or expired refresh token");
            return null;
        }

        // 更新最后使用时间
        stored.lastUsedAt = LocalDateTime.now();

        return stored.user.id.toString();
    }

    /**
     * 验证并刷新访问令牌
     */
    @Transactional
    public String validateAndRefresh(String refreshToken, String deviceInfo, String ipAddress, String userAgent) {
        String tokenHash = hashToken(refreshToken);
        
        // 检查 Token 是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(tokenHash)) {
            Log.warnf("Token is blacklisted, refresh denied: %s", tokenHash);
            return null;
        }
        
        RefreshToken stored = RefreshToken.findByTokenHash(tokenHash);

        if (stored == null || !stored.isValid()) {
            Log.warnf("Invalid or expired refresh token");
            return null;
        }

        // 更新最后使用时间
        stored.lastUsedAt = LocalDateTime.now();

        // 生成新的访问令牌
        User user = stored.user;
        Set<String> roles = user.roles.stream()
                .map(role -> role.code)
                .collect(java.util.stream.Collectors.toSet());

        return generateToken(user.username, roles);
    }

    /**
     * 撤销刷新令牌
     */
    @Transactional
    public boolean revokeRefreshToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = RefreshToken.findByTokenHash(tokenHash);

        if (stored != null) {
            stored.revoked = true;
            // 将 Token 加入黑名单，防止在剩余有效期内被使用
            tokenBlacklistService.addToBlacklist(tokenHash);
            return true;
        }
        return false;
    }

    /**
     * 撤销用户的所有刷新令牌
     */
    @Transactional
    public int revokeAllRefreshTokens(UUID userId) {
        return RefreshToken.revokeAllByUser(userId);
    }

    /**
     * 获取访问令牌过期时间（秒）
     */
    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     */
    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    /**
     * 哈希令牌
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    /**
     * 验证 JWT 令牌，返回用户 ID
     */
    public UUID validateToken(String token) {
        try {
            // 检查令牌是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                Log.warn("Token is blacklisted");
                return null;
            }
            
            // 简单验证：从 token 中解析 username claim
            // 实际项目中应使用完整的 JWT 验证流程
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.warn("Invalid token format");
                return null;
            }
            
            // 解码 payload (第二部分)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // 简单解析 JSON 获取 username (实际项目应使用 JSON 库)
            int usernameIdx = payload.indexOf("\"sub\"");
            if (usernameIdx < 0) {
                usernameIdx = payload.indexOf("\"username\"");
            }
            
            if (usernameIdx >= 0) {
                int startIdx = payload.indexOf(":", usernameIdx) + 2;
                int endIdx = payload.indexOf("\"", startIdx);
                if (startIdx > 0 && endIdx > startIdx) {
                    String username = payload.substring(startIdx, endIdx);
                    
                    // 查询用户
                    User user = User.find("username", username).firstResult();
                    if (user != null) {
                        return user.id;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            Log.warnf("Invalid token: %s", e.getMessage());
            return null;
        }
    }
}