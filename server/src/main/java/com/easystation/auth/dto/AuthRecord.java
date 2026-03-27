package com.easystation.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AuthRecord {

    // ========== 登录相关 ==========

    public record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        Boolean rememberMe,
        String mfaCode,
        String deviceInfo,
        String ipAddress,
        String userAgent
    ) {
        public LoginRequest {
            if (rememberMe == null) rememberMe = false;
        }
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String refreshToken;
        private LocalDateTime expiresAt;
        private UserBasicInfo user;
        private Boolean mfaRequired;
        private String mfaToken;
    }

    @Data
    public static class UserBasicInfo {
        private UUID id;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
        private List<String> roles;
    }

    @Data
    public static class LogoutRequest {
        private Boolean allDevices = false;
    }

    // ========== 注册相关 ==========

    public record Register(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在3-50之间") String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间") String password,
        @Email(message = "邮箱格式不正确") String email,
        String phone,
        String nickname
    ) {}

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在3-50之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间")
        private String password;

        @Email(message = "邮箱格式不正确")
        private String email;

        private String phone;
        private String nickname;
        private String verifyCode;
    }

    @Data
    public static class RegisterResponse {
        private UUID userId;
        private String username;
        private String token;
        private String refreshToken;
    }

    // 扩展登录响应，兼容 AuthService
    @Data
    public static class ExtendedLoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private String tokenType;
        private UserDto user;
        private PermDto permissions;

        public ExtendedLoginResponse(String accessToken, String refreshToken, Long expiresIn,
                                    String tokenType, UserDto user, PermDto permissions) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.tokenType = tokenType;
            this.user = user;
            this.permissions = permissions;
        }
    }

    // 用户DTO
    @Data
    public static class UserDto {
        private UUID id;
        private String username;
        private String email;
        private String phone;
        private String nickname;
        private String avatar;
        private Boolean mfaEnabled;
    }

    // 权限DTO
    @Data
    public static class PermDto {
        private List<String> roles;
        private List<String> permissions;
    }

    // ========== Token 刷新 ==========

    public record RefreshTokenRequest(
        @NotBlank(message = "刷新令牌不能为空") String refreshToken
    ) {}

    @Data
    public static class TokenResponse {
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private String tokenType;

        public TokenResponse(String token, String refreshToken, Long expiresIn, String tokenType) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.tokenType = tokenType;
        }
    }

    // ========== 密码管理 ==========

    public record ChangePasswordRequest(
        @NotBlank(message = "旧密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间") String newPassword,
        Boolean forceRelogin
    ) {
        public ChangePasswordRequest {
            if (forceRelogin == null) forceRelogin = true;
        }
    }

    // 别名，兼容 AuthService（使用 currentPassword/newPassword 风格）
    public record ChangePassword(
        @NotBlank(message = "当前密码不能为空") String currentPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间") String newPassword,
        Boolean forceLogout
    ) {
        public ChangePassword {
            if (forceLogout == null) forceLogout = true;
        }
    }

    public record ForgotPasswordRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确") String email,
        String phone
    ) {}

    public record ResetPasswordRequest(
        String email,
        String phone,
        @NotBlank(message = "令牌不能为空") String token,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间") String newPassword
    ) {}

    public record ResetPasswordVerify(
        @NotBlank(message = "令牌不能为空") String token,
        @NotBlank(message = "验证码不能为空") String verifyCode
    ) {}

    @Data
    public static class PasswordStrengthRequest {
        private String password;
    }

    @Data
    public static class PasswordStrengthResponse {
        private Integer score;
        private String level;
        private List<String> suggestions;
        private Boolean isValid;

        public PasswordStrengthResponse(Integer score, String level, List<String> suggestions, Boolean isValid) {
            this.score = score;
            this.level = level;
            this.suggestions = suggestions;
            this.isValid = isValid;
        }
    }

    // ========== 会话管理 ==========

    @Data
    public static class SessionInfo {
        private UUID id;
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessedAt;
        private Boolean isCurrent;
    }

    @Data
    public static class SessionResponse {
        private List<SessionInfo> sessions;
        private Integer total;
    }

    // ========== MFA 相关 ==========

    @Data
    public static class MfaSetupResponse {
        private String secret;
        private String qrCodeUrl;
        private List<String> recoveryCodes;
    }

    @Data
    public static class MfaVerifyRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;

        private String secret;
    }

    @Data
    public static class MfaEnableRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;
    }

    // ========== 第三方登录 ==========

    @Data
    public static class ThirdPartyBindRequest {
        @NotBlank(message = "提供商不能为空")
        private String provider;

        @NotBlank(message = "授权码不能为空")
        private String authCode;

        private String redirectUri;
    }

    @Data
    public static class ThirdPartyLoginRequest {
        @NotBlank(message = "提供商不能为空")
        private String provider;

        @NotBlank(message = "授权码不能为空")
        private String authCode;

        private String redirectUri;
    }
}