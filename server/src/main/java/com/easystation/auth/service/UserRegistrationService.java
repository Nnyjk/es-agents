package com.easystation.auth.service;

import com.easystation.auth.domain.LoginAudit;
import com.easystation.auth.domain.Session;
import com.easystation.auth.dto.AuthRecord;
import com.easystation.common.utils.PasswordUtil;
import com.easystation.system.domain.User;
import com.easystation.system.domain.enums.UserStatus;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@ApplicationScoped
public class UserRegistrationService {

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    TokenService tokenService;

    @Inject
    SessionService sessionService;

    @Transactional
    public AuthRecord.RegisterResponse register(AuthRecord.RegisterRequest request, String ipAddress, String userAgent) {
        // 验证用户名唯一性
        if (User.find("username", request.getUsername()).firstResult() != null) {
            throw new WebApplicationException("用户名已存在", 400);
        }

        // 验证邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (User.find("email", request.getEmail()).firstResult() != null) {
                throw new WebApplicationException("邮箱已被使用", 400);
            }
        }

        // 验证密码强度
        PasswordStrengthResult strength = checkPasswordStrength(request.getPassword());
        if (strength.score < 3) {
            throw new WebApplicationException("密码强度不足: " + String.join(", ", strength.suggestions), 400);
        }

        // 创建用户
        User user = new User();
        user.username = request.getUsername();
        user.password = passwordUtil.hash(request.getPassword());
        user.email = request.getEmail();
        user.phone = request.getPhone();
        user.nickname = request.getNickname() != null ? request.getNickname() : request.getUsername();
        user.status = UserStatus.ACTIVE;
        user.mfaEnabled = false;
        user.roles = new HashSet<>();
        user.persist();

        // 生成令牌
        String token = tokenService.generateToken(user.username, new HashSet<>());
        String refreshToken = tokenService.generateRefreshToken(user.id);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        // 创建会话
        sessionService.createSession(user.id, token, refreshToken, expiresAt, null, ipAddress, userAgent);

        // 记录审计日志
        recordLoginAudit(user.id, user.username, "REGISTER", "SUCCESS", "PASSWORD", ipAddress, userAgent, null);

        AuthRecord.RegisterResponse response = new AuthRecord.RegisterResponse();
        response.setUserId(user.id);
        response.setUsername(user.username);
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Transactional
    public AuthRecord.LoginResponse login(AuthRecord.LoginRequest request) {
        User user = User.find("username", request.getUsername()).firstResult();

        // 检查失败次数
        LocalDateTime lockTime = LocalDateTime.now().minusMinutes(15);
        long failedCount = LoginAudit.countFailedAttempts(request.getUsername(), lockTime);
        if (failedCount >= 5) {
            recordLoginAudit(user != null ? user.id : null, request.getUsername(), "LOGIN", "FAILED", 
                "PASSWORD", request.getIpAddress(), request.getUserAgent(), "账户已被锁定，请15分钟后重试");
            throw new WebApplicationException("账户已被锁定，请稍后重试", 429);
        }

        if (user == null) {
            recordLoginAudit(null, request.getUsername(), "LOGIN", "FAILED", 
                "PASSWORD", request.getIpAddress(), request.getUserAgent(), "用户不存在");
            throw new WebApplicationException("用户名或密码错误", 401);
        }

        if (!passwordUtil.check(request.getPassword(), user.password)) {
            recordLoginAudit(user.id, user.getUsername(), "LOGIN", "FAILED", 
                "PASSWORD", request.getIpAddress(), request.getUserAgent(), "密码错误");
            throw new WebApplicationException("用户名或密码错误", 401);
        }

        if (user.status != UserStatus.ACTIVE) {
            recordLoginAudit(user.id, user.getUsername(), "LOGIN", "FAILED", 
                "PASSWORD", request.getIpAddress(), request.getUserAgent(), "账户状态异常: " + user.status);
            throw new WebApplicationException("账户状态异常: " + user.status, 403);
        }

        // 检查MFA
        if (Boolean.TRUE.equals(user.mfaEnabled)) {
            if (request.getMfaCode() == null || request.getMfaCode().isBlank()) {
                String mfaToken = tokenService.generateMfaToken(user.id);
                AuthRecord.LoginResponse response = new AuthRecord.LoginResponse();
                response.setMfaRequired(true);
                response.setMfaToken(mfaToken);
                return response;
            }
            // TODO: 验证MFA码
        }

        // 生成令牌
        java.util.Set<String> roleCodes = user.roles.stream()
            .map(r -> r.code)
            .collect(java.util.stream.Collectors.toSet());
        String token = tokenService.generateToken(user.username, roleCodes);
        String refreshToken = tokenService.generateRefreshToken(user.id);
        LocalDateTime expiresAt = request.getRememberMe() != null && request.getRememberMe() 
            ? LocalDateTime.now().plusDays(30) 
            : LocalDateTime.now().plusHours(24);

        // 创建会话
        Session session = sessionService.createSession(user.id, token, refreshToken, expiresAt, 
            request.getDeviceInfo(), request.getIpAddress(), request.getUserAgent());

        // 更新用户最后登录时间
        user.lastLoginAt = LocalDateTime.now();
        user.persist();

        // 记录审计日志
        recordLoginAudit(user.id, user.username, "LOGIN", "SUCCESS", 
            "PASSWORD", request.getIpAddress(), request.getUserAgent(), null);

        // 构建响应
        AuthRecord.LoginResponse response = new AuthRecord.LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresAt(expiresAt);
        response.setMfaRequired(false);

        AuthRecord.UserBasicInfo userInfo = new AuthRecord.UserBasicInfo();
        userInfo.setId(user.id);
        userInfo.setUsername(user.username);
        userInfo.setEmail(user.email);
        userInfo.setPhone(user.phone);
        userInfo.setNickname(user.nickname);
        userInfo.setAvatar(user.avatar);
        userInfo.setRoles(roleCodes.stream().toList());
        response.setUser(userInfo);

        return response;
    }

    @Transactional
    public void logout(String token, UUID userId, boolean allDevices, String ipAddress) {
        if (allDevices) {
            Session.invalidateByUserId(userId);
            recordLoginAudit(userId, null, "LOGOUT", "SUCCESS", 
                "ALL_DEVICES", ipAddress, null, "退出所有设备");
        } else {
            Session session = Session.findByToken(token);
            if (session != null) {
                session.isActive = false;
                session.logoutAt = LocalDateTime.now();
                session.logoutReason = "LOGOUT";
                session.persist();
            }
            recordLoginAudit(userId, null, "LOGOUT", "SUCCESS", 
                "SINGLE", ipAddress, null, null);
        }
    }

    @Transactional
    public AuthRecord.TokenResponse refreshToken(String refreshToken) {
        Session session = Session.findByRefreshToken(refreshToken);
        if (session == null || !session.isActive) {
            throw new WebApplicationException("无效的刷新令牌", 401);
        }

        if (session.refreshExpiresAt != null && session.refreshExpiresAt.isBefore(LocalDateTime.now())) {
            throw new WebApplicationException("刷新令牌已过期", 401);
        }

        User user = User.findById(session.userId);
        if (user == null || user.status != UserStatus.ACTIVE) {
            throw new WebApplicationException("用户状态异常", 403);
        }

        // 生成新令牌
        java.util.Set<String> roleCodes = user.roles.stream()
            .map(r -> r.code)
            .collect(java.util.stream.Collectors.toSet());
        String newToken = tokenService.generateToken(user.username, roleCodes);
        String newRefreshToken = tokenService.generateRefreshToken(user.id);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        // 更新会话
        session.token = newToken;
        session.refreshToken = newRefreshToken;
        session.expiresAt = expiresAt;
        session.lastActivityAt = LocalDateTime.now();
        session.persist();

        AuthRecord.TokenResponse response = new AuthRecord.TokenResponse();
        response.setToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresAt(expiresAt);
        return response;
    }

    private void recordLoginAudit(UUID userId, String username, String action, String result, 
            String method, String ipAddress, String userAgent, String failureReason) {
        LoginAudit audit = new LoginAudit();
        audit.userId = userId;
        audit.username = username;
        audit.action = action;
        audit.result = result;
        audit.loginMethod = method;
        audit.ipAddress = ipAddress;
        audit.userAgent = userAgent;
        audit.failureReason = failureReason;
        audit.persist();
    }

    private PasswordStrengthResult checkPasswordStrength(String password) {
        PasswordStrengthResult result = new PasswordStrengthResult();
        result.score = 0;
        result.suggestions = new java.util.ArrayList<>();

        if (password.length() >= 8) result.score++;
        if (password.length() >= 12) result.score++;
        if (password.matches(".*[A-Z].*")) result.score++;
        if (password.matches(".*[a-z].*")) result.score++;
        if (password.matches(".*[0-9].*")) result.score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) result.score++;

        if (result.score < 3) {
            if (password.length() < 8) result.suggestions.add("密码长度至少8位");
            if (!password.matches(".*[A-Z].*")) result.suggestions.add("包含大写字母");
            if (!password.matches(".*[a-z].*")) result.suggestions.add("包含小写字母");
            if (!password.matches(".*[0-9].*")) result.suggestions.add("包含数字");
            if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) result.suggestions.add("包含特殊字符");
        }

        return result;
    }

    private static class PasswordStrengthResult {
        int score;
        java.util.List<String> suggestions;
    }
}