package com.easystation.auth.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class TokenService {

    private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 86400; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800; // 7 days
    private static final long MFA_TOKEN_EXPIRY_SECONDS = 300; // 5 minutes

    public String generateToken(String username, Set<String> roles) {
        return Jwt.issuer("https://easystation.com/issuer")
                .upn(username)
                .groups(roles)
                .expiresIn(ACCESS_TOKEN_EXPIRY_SECONDS)
                .sign();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwt.issuer("https://easystation.com/issuer")
                .upn(userId.toString())
                .claim("type", "refresh")
                .expiresIn(REFRESH_TOKEN_EXPIRY_SECONDS)
                .sign();
    }

    public String generateMfaToken(UUID userId) {
        return Jwt.issuer("https://easystation.com/issuer")
                .upn(userId.toString())
                .claim("type", "mfa")
                .expiresIn(MFA_TOKEN_EXPIRY_SECONDS)
                .sign();
    }

    public String generateTokenFromRefreshToken(String refreshToken, Set<String> roles) {
        // Validate refresh token and generate new access token
        // This is a simplified implementation
        return Jwt.issuer("https://easystation.com/issuer")
                .upn(refreshToken)
                .groups(roles)
                .expiresIn(ACCESS_TOKEN_EXPIRY_SECONDS)
                .sign();
    }
}
