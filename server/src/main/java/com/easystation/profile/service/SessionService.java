package com.easystation.profile.service;

import com.easystation.profile.dto.SessionRecord;
import com.easystation.profile.domain.UserSession;
import com.easystation.profile.mapper.SessionMapper;
import com.easystation.profile.repository.SessionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class SessionService {

    @Inject
    SessionRepository sessionRepository;

    @Inject
    SessionMapper sessionMapper;

    public List<SessionRecord> listSessions(UUID userId, String currentTokenId) {
        // Clean up expired sessions first
        sessionRepository.deleteAllExpired(userId, LocalDateTime.now());
        
        List<UserSession> sessions = sessionRepository.findByUserIdOrderByLastActivityAtDesc(userId);
        return sessions.stream()
            .map(s -> sessionMapper.toRecord(s, currentTokenId))
            .collect(Collectors.toList());
    }

    public SessionRecord.Summary getSessionSummary(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        
        int total = (int) sessionRepository.countByUserId(userId);
        int active = (int) sessionRepository.countActiveSessions(userId, now);
        
        return new SessionRecord.Summary(total, active);
    }

    @Transactional
    public void terminateSession(UUID userId, UUID sessionId, String currentTokenId) {
        UserSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new WebApplicationException("Session not found", Response.Status.NOT_FOUND));
        
        // Don't allow terminating current session this way
        if (currentTokenId != null && currentTokenId.equals(session.tokenId)) {
            throw new WebApplicationException("Cannot terminate current session", Response.Status.BAD_REQUEST);
        }
        
        sessionRepository.deleteByIdAndUserId(sessionId, userId);
    }

    @Transactional
    public void terminateAllOtherSessions(UUID userId, String currentTokenId) {
        // If we have current token ID, exclude it
        if (currentTokenId != null) {
            sessionRepository.deleteAllByUserIdExceptTokenId(userId, currentTokenId);
        } else {
            // Delete all sessions for this user (dangerous!)
            sessionRepository.delete("userId", userId);
        }
    }
}