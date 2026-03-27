package com.easystation.auth.service;

import com.easystation.auth.domain.LoginAudit;
import com.easystation.auth.domain.PasswordReset;
import com.easystation.auth.dto.AuthRecord;
import com.easystation.common.utils.PasswordUtil;
import com.easystation.profile.domain.UserPasswordHistory;
import com.easystation.system.domain.User;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PasswordService {

    @Inject
    PasswordUtil passwordUtil;

    @Transactional
    public void changePassword(UUID userId, AuthRecord.ChangePasswordRequest request, String ipAddress) {
        User user = User.findById(userId);
        if (user == null) {
            throw new WebApplicationException("用户不存在", 404);
        }

        // 验证旧密码
        if (!passwordUtil.check(request.oldPassword(), user.password)) {
            throw new WebApplicationException("旧密码错误", 400);
        }

        // 验证新密码强度
        AuthRecord.PasswordStrengthResponse strength = checkPasswordStrength(request.newPassword());
        if (strength.getScore() < 3) {
            throw new WebApplicationException("新密码强度不足", 400);
        }

        // 检查密码历史
        if (isPasswordInHistory(userId, request.newPassword())) {
            throw new WebApplicationException("新密码不能与近期使用过的密码相同", 400);
        }

        // 保存密码历史
        UserPasswordHistory history = new UserPasswordHistory();
        history.userId = userId;
        history.passwordHash = user.password;
        history.persist();

        // 更新密码
        user.password = passwordUtil.hash(request.newPassword());
        user.passwordChangedAt = LocalDateTime.now();
        user.persist();

        // 记录审计
        LoginAudit audit = new LoginAudit();
        audit.userId = userId;
        audit.username = user.username;
        audit.action = "CHANGE_PASSWORD";
        audit.result = "SUCCESS";
        audit.ipAddress = ipAddress;
        audit.persist();

        // 如果需要强制重新登录，使所有会话失效
        if (Boolean.TRUE.equals(request.forceRelogin())) {
            // Session.invalidateByUserId(userId); // 可选
        }
    }

    @Transactional
    public String initiatePasswordReset(String email, String ipAddress) {
        User user = User.find("email", email).firstResult();
        if (user == null) {
            // 不暴露用户是否存在
            Log.infof("Password reset requested for non-existent email: %s", email);
            return null;
        }

        // 生成重置令牌
        String token = generateSecureToken();
        
        PasswordReset reset = new PasswordReset();
        reset.userId = user.id;
        reset.token = token;
        reset.resetType = "FORGOT";
        reset.expiresAt = LocalDateTime.now().plusHours(1);
        reset.ipAddress = ipAddress;
        reset.persist();

        // 记录审计
        LoginAudit audit = new LoginAudit();
        audit.userId = user.id;
        audit.username = user.username;
        audit.action = "PASSWORD_RESET_REQUEST";
        audit.result = "SUCCESS";
        audit.ipAddress = ipAddress;
        audit.persist();

        return token;
    }

    @Transactional
    public void resetPassword(AuthRecord.ResetPasswordRequest request, String ipAddress) {
        PasswordReset reset = PasswordReset.findValidToken(request.token());
        if (reset == null) {
            throw new WebApplicationException("无效或已过期的重置令牌", 400);
        }

        User user = User.findById(reset.userId);
        if (user == null) {
            throw new WebApplicationException("用户不存在", 404);
        }

        // 验证密码强度
        AuthRecord.PasswordStrengthResponse strength = checkPasswordStrength(request.newPassword());
        if (strength.getScore() < 3) {
            throw new WebApplicationException("密码强度不足", 400);
        }

        // 检查密码历史
        if (isPasswordInHistory(user.id, request.newPassword())) {
            throw new WebApplicationException("新密码不能与近期使用过的密码相同", 400);
        }

        // 保存密码历史
        UserPasswordHistory history = new UserPasswordHistory();
        history.userId = user.id;
        history.passwordHash = user.password;
        history.persist();

        // 更新密码
        user.password = passwordUtil.hash(request.newPassword());
        user.passwordChangedAt = LocalDateTime.now();
        user.persist();

        // 标记令牌已使用
        reset.isUsed = true;
        reset.usedAt = LocalDateTime.now();
        reset.persist();

        // 记录审计
        LoginAudit audit = new LoginAudit();
        audit.userId = user.id;
        audit.username = user.username;
        audit.action = "PASSWORD_RESET";
        audit.result = "SUCCESS";
        audit.ipAddress = ipAddress;
        audit.persist();
    }

    public AuthRecord.PasswordStrengthResponse checkPasswordStrength(String password) {
        List<String> suggestions = new ArrayList<>();
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;

        if (password.length() < 8) suggestions.add("密码长度至少8位");
        if (!password.matches(".*[A-Z].*")) suggestions.add("包含大写字母");
        if (!password.matches(".*[a-z].*")) suggestions.add("包含小写字母");
        if (!password.matches(".*[0-9].*")) suggestions.add("包含数字");
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) suggestions.add("包含特殊字符");

        // 设置强度等级
        String level;
        if (score <= 2) {
            level = "弱";
        } else if (score <= 4) {
            level = "中";
        } else {
            level = "强";
        }

        boolean isValid = score >= 3;
        return new AuthRecord.PasswordStrengthResponse(score, level, suggestions, isValid);
    }

    private boolean isPasswordInHistory(UUID userId, String newPassword) {
        List<UserPasswordHistory> history = UserPasswordHistory.find("userId", userId)
            .range(0, 4) // 检查最近5个密码
            .list();
        
        for (UserPasswordHistory h : history) {
            if (passwordUtil.check(newPassword, h.passwordHash)) {
                return true;
            }
        }
        return false;
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}