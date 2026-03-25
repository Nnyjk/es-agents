package com.easystation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OAuthRecord {

    // ========== 第三方登录 ==========

    @Data
    public static class OAuthCallbackRequest {
        @NotBlank(message = "Provider不能为空")
        private String provider;

        @NotBlank(message = "授权码不能为空")
        private String code;

        private String state;
        private String redirectUri;
    }

    @Data
    public static class OAuthUrlResponse {
        private String authorizationUrl;
        private String state;
    }

    @Data
    public static class ThirdPartyLoginInfo {
        private UUID id;
        private String provider;
        private String providerUsername;
        private String providerEmail;
        private String providerAvatar;
        private LocalDateTime createdAt;
    }

    // ========== SSO 相关 ==========

    @Data
    public static class SsoLoginRequest {
        @NotBlank(message = "SSO名称不能为空")
        private String ssoName;

        private String samlResponse;
        private String relayState;
    }

    @Data
    public static class SsoConfigRequest {
        @NotBlank(message = "名称不能为空")
        private String name;

        @NotBlank(message = "显示名称不能为空")
        private String displayName;

        @NotBlank(message = "SSO类型不能为空")
        private String ssoType;

        private Boolean isEnabled = false;

        // SAML
        private String ssoUrl;
        private String sloUrl;
        private String certificate;
        private String entityId;

        // OAuth2/OIDC
        private String clientId;
        private String clientSecret;
        private String authorizationUrl;
        private String tokenUrl;
        private String userinfoUrl;
        private String issuerUrl;
        private String scope;
        private String callbackUrl;

        private Boolean autoCreateUser = true;
        private String autoAssignRoles;
        private String description;
    }

    @Data
    public static class SsoConfigResponse {
        private UUID id;
        private String name;
        private String displayName;
        private String ssoType;
        private Boolean isEnabled;
        private String ssoUrl;
        private String entityId;
        private String clientId;
        private String authorizationUrl;
        private String callbackUrl;
        private Boolean autoCreateUser;
        private String autoAssignRoles;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ========== 登录审计 ==========

    @Data
    public static class LoginAuditResponse {
        private UUID id;
        private String username;
        private String action;
        private String result;
        private String loginMethod;
        private String ipAddress;
        private String deviceInfo;
        private String failureReason;
        private LocalDateTime createdAt;
    }

    @Data
    public static class LoginAuditQueryRequest {
        private String username;
        private String action;
        private String result;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer page = 1;
        private Integer size = 20;
    }
}