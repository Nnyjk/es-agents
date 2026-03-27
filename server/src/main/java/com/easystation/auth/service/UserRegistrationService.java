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
        if (request.getPassword().length() < 8) {
            throw new WebApplicationException("密码长度至少8位", 400);
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
        String refreshToken = tokenService.generateRefreshToken(user, null, ipAddress, userAgent);
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
    public void logout(String token, UUID userId, boolean allDevices, String ipAddress) {
        if (token != null) {
            sessionService.invalidateByToken(token);
            Log.infof("User %s logged out from token", userId);
        }

        if (allDevices) {
            sessionService.invalidateAllSessions(userId);
            Log.infof("User %s logged out from all devices", userId);
        }

        recordLoginAudit(userId, null, "LOGOUT", "SUCCESS", null, ipAddress, null, null);
    }

    @Transactional
    public AuthRecord.TokenResponse refreshToken(String refreshToken) {
        // 验证并刷新令牌
        String userId = tokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new WebApplicationException("无效的刷新令牌", 401);
        }

        User user = User.findById(UUID.fromString(userId));
        if (user == null) {
            throw new WebApplicationException("用户不存在", 404);
        }

        // 生成新令牌
        String newToken = tokenService.generateToken(user.username, new HashSet<>());
        String newRefreshToken = tokenService.generateRefreshToken(user, null, "unknown", "refresh");

        return new AuthRecord.TokenResponse(newToken, newRefreshToken, 86400L, "Bearer");
    }

    private void recordLoginAudit(UUID userId, String username, String action, String result,
                                 String loginMethod, String ipAddress, String userAgent, String failureReason) {
        LoginAudit audit = new LoginAudit();
        audit.userId = userId;
        audit.username = username;
        audit.action = action;
        audit.result = result;
        audit.loginMethod = loginMethod;
        audit.ipAddress = ipAddress;
        audit.userAgent = userAgent;
        audit.failureReason = failureReason;
        audit.persist();
    }
}