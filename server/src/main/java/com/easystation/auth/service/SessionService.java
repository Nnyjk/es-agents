package com.easystation.auth.service;

import com.easystation.auth.domain.Session;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class SessionService {

    @Transactional
    public Session createSession(UUID userId, String token, String refreshToken, 
            LocalDateTime expiresAt, String deviceInfo, String ipAddress, String userAgent) {
        Session session = new Session();
        session.userId = userId;
        session.token = token;
        session.refreshToken = refreshToken;
        session.expiresAt = expiresAt;
        session.refreshExpiresAt = expiresAt.plusDays(7);
        session.deviceInfo = deviceInfo;
        session.ipAddress = ipAddress;
        session.userAgent = userAgent;
        session.lastActivityAt = LocalDateTime.now();
        session.isActive = true;
        session.persist();
        return session;
    }

    public List<Session> getActiveSessions(UUID userId) {
        return Session.findActiveByUserId(userId);
    }

    public List<Session> getAllSessions(UUID userId) {
        return Session.findByUserId(userId);
    }

    @Transactional
    public void invalidateSession(UUID sessionId, UUID userId) {
        Session session = Session.findById(sessionId);
        if (session != null && session.userId.equals(userId)) {
            session.isActive = false;
            session.logoutAt = LocalDateTime.now();
            session.logoutReason = "MANUAL_INVALIDATE";
            session.persist();
        }
    }

    @Transactional
    public void invalidateAllSessions(UUID userId) {
        Session.invalidateByUserId(userId);
    }

    @Transactional
    public void invalidateByToken(String token) {
        Session session = Session.findByToken(token);
        if (session != null) {
            session.isActive = false;
            session.logoutAt = LocalDateTime.now();
            session.logoutReason = "TOKEN_INVALIDATE";
            session.persist();
        }
    }

    @Transactional
    public void updateActivity(String token) {
        Session session = Session.findByToken(token);
        if (session != null && session.isActive) {
            session.lastActivityAt = LocalDateTime.now();
            session.persist();
        }
    }

    @Transactional
    public void cleanupExpiredSessions() {
        long count = Session.update("isActive = false where isActive = true and expiresAt < ?1", 
            LocalDateTime.now());
        Log.infof("Cleaned up %d expired sessions", count);
    }

    public boolean isSessionValid(String token) {
        Session session = Session.findByToken(token);
        return session != null && session.isActive && session.expiresAt.isAfter(LocalDateTime.now());
    }
}