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
@Table(name = "auth_sso_config")
@Getter
@Setter
public class SsoConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true, length = 50)
    public String name;

    @Column(name = "display_name", nullable = false)
    public String displayName;

    @Column(name = "sso_type", nullable = false, length = 20)
    public String ssoType;

    @Column(name = "is_enabled")
    public Boolean isEnabled = false;

    // SAML 配置
    @Column(name = "sso_url", length = 500)
    public String ssoUrl;

    @Column(name = "slo_url", length = 500)
    public String sloUrl;

    @Column(columnDefinition = "TEXT")
    public String certificate;

    @Column(name = "entity_id", length = 500)
    public String entityId;

    // OAuth2/OIDC 配置
    @Column(name = "client_id", length = 200)
    public String clientId;

    @Column(name = "client_secret")
    public String clientSecret;

    @Column(name = "authorization_url", length = 500)
    public String authorizationUrl;

    @Column(name = "token_url", length = 500)
    public String tokenUrl;

    @Column(name = "userinfo_url", length = 500)
    public String userinfoUrl;

    @Column(name = "issuer_url", length = 500)
    public String issuerUrl;

    @Column(name = "scope", length = 200)
    public String scope;

    @Column(name = "callback_url", length = 500)
    public String callbackUrl;

    @Column(name = "auto_create_user")
    public Boolean autoCreateUser = true;

    @Column(name = "auto_assign_roles")
    public String autoAssignRoles;

    @Column(columnDefinition = "TEXT")
    public String description;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public static SsoConfig findByName(String name) {
        return find("name", name).firstResult();
    }

    public static java.util.List<SsoConfig> findEnabled() {
        return list("isEnabled", true);
    }
}