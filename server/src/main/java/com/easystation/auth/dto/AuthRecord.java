package com.easystation.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * 认证相关的请求/响应 DTO
 */
public class AuthRecord {

    /**
     * 用户注册请求
     */
    public record Register(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和连字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须在 8-128 个字符之间")
        String password,

        @Email(message = "邮箱格式不正确")
        String email,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        String nickname
    ) {}

    /**
     * 用户登录请求（扩展现有的 Login record）
     */
    public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password,

        Boolean rememberMe  // 记住登录状态
    ) {}

    /**
     * 扩展登录响应（包含刷新令牌）
     */
    public record ExtendedLoginResponse(
        String token,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        Object userInfo,
        Object permissions
    ) {}

    /**
     * Token 刷新请求
     */
    public record RefreshTokenRequest(
        @NotBlank(message = "刷新令牌不能为空")
        String refreshToken
    ) {}

    /**
     * Token 刷新响应
     */
    public record TokenResponse(
        String token,
        String refreshToken,
        Long expiresIn,
        String tokenType
    ) {}

    /**
     * 密码修改请求
     */
    public record ChangePassword(
        @NotBlank(message = "当前密码不能为空")
        String currentPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须在 8-128 个字符之间")
        String newPassword,

        Boolean forceLogout  // 是否强制登出所有设备
    ) {}

    /**
     * 密码重置请求（通过邮箱或手机）
     */
    public record ResetPasswordRequest(
        String email,
        String phone
    ) {}

    /**
     * 密码重置验证
     */
    public record ResetPasswordVerify(
        @NotBlank(message = "验证码不能为空")
        String verifyCode,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须在 8-128 个字符之间")
        String newPassword
    ) {}

    /**
     * 密码强度检查请求
     */
    public record CheckPasswordStrength(
        @NotBlank(message = "密码不能为空")
        String password
    ) {}

    /**
     * 密码强度检查响应
     */
    public record PasswordStrengthResponse(
        int score,
        String level,
        java.util.List<String> hints,
        boolean acceptable
    ) {}

    /**
     * 登出请求
     */
    public record Logout(
        Boolean allDevices  // 是否登出所有设备
    ) {}

    /**
     * 登录失败响应
     */
    public record LoginFailedResponse(
        String message,
        Integer remainingAttempts,
        Long lockUntil
    ) {}
}