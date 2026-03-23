package com.easystation.profile.repository;

import com.easystation.profile.domain.UserSession;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionRepository implements PanacheRepositoryBase<UserSession, UUID> {

    public List<UserSession> findByUserIdOrderByLastActivityAtDesc(UUID userId) {
        return find("userId ORDER BY lastActivityAt DESC", userId).list();
    }

    public long countByUserId(UUID userId) {
        return count("userId", userId);
    }

    public long countActiveSessions(UUID userId, LocalDateTime now) {
        return count("userId = ?1 AND isActive = true AND expiresAt > ?2", userId, now);
    }

    public Optional<UserSession> findByIdAndUserId(UUID id, UUID userId) {
        return find("id = ?1 AND userId = ?2", id, userId).firstResultOptional();
    }

    public void deleteByIdAndUserId(UUID id, UUID userId) {
        delete("id = ?1 AND userId = ?2", id, userId);
    }

    public void deleteAllByUserIdExceptTokenId(UUID userId, String excludeTokenId) {
        delete("userId = ?1 AND tokenId != ?2", userId, excludeTokenId);
    }

    public void deleteAllExpired(UUID userId, LocalDateTime now) {
        delete("userId = ?1 AND (expiresAt < ?2 OR isActive = false)", userId, now);
    }
}