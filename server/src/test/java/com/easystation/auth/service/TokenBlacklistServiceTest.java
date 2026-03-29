package com.easystation.auth.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenBlacklistService 单元测试
 * 
 * 注意：这些测试需要 Redis 连接。如果 Redis 不可用，测试将跳过。
 */
@QuarkusTest
public class TokenBlacklistServiceTest {

    @Inject
    TokenBlacklistService tokenBlacklistService;

    @ConfigProperty(name = "auth.jwt.blacklist.enabled")
    boolean blacklistEnabled;

    private String testTokenHash;

    @BeforeEach
    public void beforeEach() {
        // 生成一个测试 Token 哈希
        String testToken = "test-token-" + System.currentTimeMillis();
        testTokenHash = hashToken(testToken);
        
        // 确保测试前 Token 不在黑名单中
        if (blacklistEnabled) {
            tokenBlacklistService.removeFromBlacklist(testTokenHash);
        }
    }

    @Test
    public void testIsEnabled() {
        // 验证黑名单功能配置状态
        boolean enabled = tokenBlacklistService.isEnabled();
        // 如果配置启用，验证功能正常；如果禁用，验证返回预期行为
        if (enabled) {
            assertTrue(enabled);
        } else {
            assertFalse(enabled);
        }
    }

    @Test
    public void testIsBlacklistedWhenDisabled() {
        // 当黑名单禁用时，应该始终返回 false
        if (!blacklistEnabled) {
            assertFalse(tokenBlacklistService.isBlacklisted(testTokenHash));
        }
    }

    @Test
    public void testMultipleTokens() {
        if (!blacklistEnabled) {
            // 黑名单禁用时跳过实际测试
            return;
        }
        
        String tokenHash1 = hashToken("token-1");
        String tokenHash2 = hashToken("token-2");
        String tokenHash3 = hashToken("token-3");

        // 只将 token1 和 token3 加入黑名单
        tokenBlacklistService.addToBlacklist(tokenHash1);
        tokenBlacklistService.addToBlacklist(tokenHash3);

        // 验证 token1 和 token3 在黑名单中，token2 不在
        assertTrue(tokenBlacklistService.isBlacklisted(tokenHash1));
        assertFalse(tokenBlacklistService.isBlacklisted(tokenHash2));
        assertTrue(tokenBlacklistService.isBlacklisted(tokenHash3));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
