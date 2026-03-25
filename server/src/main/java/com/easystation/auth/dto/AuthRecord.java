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

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        private Boolean rememberMe = false;
        private String mfaCode;
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
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
        private String email;
        private String phone;
        private String nickname;
        private String avatar;
        private List<String> roles;
        private List<String> permissions;
    }

    @Data
    public static class LogoutRequest {
        private String token;
        private Boolean allDevices = false;
    }

    // ========== 注册相关 ==========

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在3-50之间")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "用户名必须以字母开头，只能包含字母、数字、下划线和连字符")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间")
        private String password;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
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

    // ========== Token 刷新 ==========

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "刷新令牌不能为空")
        private String refreshToken;
    }

    @Data
    public static class TokenResponse {
        private String token;
        private String refreshToken;
        private LocalDateTime expiresAt;
    }

    // ========== 密码管理 ==========

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间")
        private String newPassword;

        private Boolean forceRelogin = true;
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "令牌不能为空")
        private String token;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度需在8-100之间")
        private String newPassword;
    }

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
    }

    // ========== 会话管理 ==========

    @Data
    public static class SessionInfo {
        private UUID id;
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime createdAt;
        private LocalDateTime lastActivityAt;
        private LocalDateTime expiresAt;
        private Boolean isActive;
        private Boolean isCurrent;
    }

    // ========== MFA 相关 ==========

    @Data
    public static class MfaSetupResponse {
        private String secret;
        private String qrCodeUrl;
        private List<String> backupCodes;
    }

    @Data
    public static class MfaVerifyRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;

        private String mfaToken;
    }

    @Data
    public static class MfaEnableRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;
    }
}