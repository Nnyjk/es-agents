package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_third_party_login")
@Getter
@Setter
public class ThirdPartyLogin extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "provider", nullable = false, length = 50)
    public String provider;

    @Column(name = "provider_user_id", nullable = false)
    public String providerUserId;

    @Column(name = "provider_username")
    public String providerUsername;

    @Column(name = "provider_email")
    public String providerEmail;

    @Column(name = "provider_avatar")
    public String providerAvatar;

    @Column(name = "access_token")
    public String accessToken;

    @Column(name = "refresh_token")
    public String refreshToken;

    @Column(name = "token_expires_at")
    public LocalDateTime tokenExpiresAt;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public static ThirdPartyLogin findByProviderAndUserId(String provider, String providerUserId) {
        return find("provider = ?1 and providerUserId = ?2", provider, providerUserId).firstResult();
    }

    public static ThirdPartyLogin findByUserIdAndProvider(UUID userId, String provider) {
        return find("userId = ?1 and provider = ?2", userId, provider).firstResult();
    }

    public static java.util.List<ThirdPartyLogin> findByUserId(UUID userId) {
        return list("userId", userId);
    }
}