package com.easystation.auth.service;

import com.easystation.auth.domain.LoginAttempt;
import com.easystation.auth.domain.PasswordResetToken;
import com.easystation.auth.domain.RefreshToken;
import com.easystation.auth.dto.AuthRecord;
import com.easystation.auth.record.LoginResponse;
import com.easystation.auth.record.RouteRecord;
import com.easystation.common.utils.PasswordStrengthUtil;
import com.easystation.common.utils.PasswordUtil;
import com.easystation.system.domain.Module;
import com.easystation.system.domain.Role;
import com.easystation.system.domain.User;
import com.easystation.system.domain.enums.ModuleType;
import com.easystation.system.domain.enums.UserStatus;
import com.easystation.system.record.RoleRecord;
import com.easystation.system.record.UserRecord;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    PasswordStrengthUtil passwordStrengthUtil;

    @Inject
    TokenService tokenService;

    @ConfigProperty(name = "auth.login.max-failed-attempts", defaultValue = "5")
    int maxFailedAttempts;

    @ConfigProperty(name = "auth.login.lock-duration-minutes", defaultValue = "30")
    int lockDurationMinutes;

    /**
     * 用户注册
     */
    @Transactional
    public UserRecord register(AuthRecord.Register request) {
        // 检查用户名是否已存在
        if (User.find("username", request.username()).firstResult() != null) {
            throw new WebApplicationException("用户名已存在", Response.Status.CONFLICT.getStatusCode());
        }

        // 检查邮箱是否已存在
        if (request.email() != null && !request.email().isBlank()) {
            if (User.find("email", request.email()).firstResult() != null) {
                throw new WebApplicationException("邮箱已被注册", Response.Status.CONFLICT.getStatusCode());
            }
        }

        // 检查手机号是否已存在
        if (request.phone() != null && !request.phone().isBlank()) {
            if (User.find("phone", request.phone()).firstResult() != null) {
                throw new WebApplicationException("手机号已被注册", Response.Status.CONFLICT.getStatusCode());
            }
        }

        // 验证密码强度
        var strengthResult = passwordStrengthUtil.validate(request.password());
        if (!strengthResult.acceptable()) {
            throw new WebApplicationException("密码强度不足：" + String.join("; ", strengthResult.hints()),
                Response.Status.BAD_REQUEST.getStatusCode());
        }

        // 创建用户
        User user = new User();
        user.username = request.username();
        user.password = passwordUtil.hash(request.password());
        user.email = request.email();
        user.phone = request.phone();
        user.nickname = request.nickname() != null ? request.nickname() : request.username();
        user.status = UserStatus.ACTIVE;
        user.mfaEnabled = false;

        // 分配默认角色（如果有）
        Role defaultRole = Role.find("code", "user").firstResult();
        if (defaultRole != null) {
            user.roles = new HashSet<>();
            user.roles.add(defaultRole);
        }

        user.persist();

        Log.infof("User registered: %s", user.username);

        return new UserRecord(user.id, user.username, user.status,
            user.roles.stream().map(r -> new RoleRecord(r.id, r.code, r.name, r.description, null, null))
                .collect(Collectors.toSet()));
    }

    /**
     * 用户登录
     */
    @Transactional
    public AuthRecord.ExtendedLoginResponse login(AuthRecord.LoginRequest request, String ipAddress, String userAgent) {
        User user = User.find("username", request.username()).firstResult();

        if (user == null) {
            LoginAttempt.fail(request.username(), ipAddress, userAgent, "用户不存在");
            throw new WebApplicationException("用户名或密码错误", Response.Status.UNAUTHORIZED.getStatusCode());
        }

        // 检查账户是否被锁定
        if (user.lockedUntil != null && user.lockedUntil.isAfter(LocalDateTime.now())) {
            LoginAttempt.fail(request.username(), ipAddress, userAgent, "账户已锁定");
            throw new WebApplicationException(
                String.format("账户已锁定，请在 %s 后重试", user.lockedUntil.toString()),
                Response.Status.FORBIDDEN.getStatusCode()
            );
        }

        // 验证密码
        if (!passwordUtil.check(request.password(), user.password)) {
            handleFailedLogin(user, request.username(), ipAddress, userAgent);
            throw new WebApplicationException("用户名或密码错误", Response.Status.UNAUTHORIZED.getStatusCode());
        }

        // 检查用户状态
        if (user.status != UserStatus.ACTIVE) {
            LoginAttempt.fail(request.username(), ipAddress, userAgent, "账户状态异常: " + user.status);
            throw new WebApplicationException("账户状态异常：" + user.status, Response.Status.FORBIDDEN.getStatusCode());
        }

        // 登录成功，重置失败计数
        user.failedLoginCount = 0;
        user.lockedUntil = null;

        // 记录登录成功
        LoginAttempt.success(request.username(), ipAddress, userAgent);

        // 生成令牌
        Set<String> roleCodes = user.roles.stream().map(r -> r.code).collect(Collectors.toSet());
        boolean rememberMe = request.rememberMe() != null && request.rememberMe();
        String token = tokenService.generateToken(user.username, roleCodes, rememberMe);
        String refreshToken = tokenService.generateRefreshToken(user, null, ipAddress, userAgent);

        // 构建响应
        Set<RoleRecord> roleDtos = user.roles.stream().map(r -> new RoleRecord(
            r.id, r.code, r.name, r.description, null, null
        )).collect(Collectors.toSet());

        UserRecord userDto = new UserRecord(user.id, user.username, user.status, roleDtos);

        Set<String> menus = new HashSet<>();
        Set<String> actions = new HashSet<>();
        user.roles.forEach(role -> {
            role.modules.forEach(m -> menus.add(m.code));
            role.actions.forEach(a -> actions.add(a.code));
        });

        LoginResponse.PermissionRecord permDto = new LoginResponse.PermissionRecord(menus, actions);

        Log.infof("User logged in: %s from %s", user.username, ipAddress);

        return new AuthRecord.ExtendedLoginResponse(token, refreshToken, tokenService.getAccessTokenExpiresIn(),
            "Bearer", userDto, permDto);
    }

    /**
     * 处理登录失败
     */
    private void handleFailedLogin(User user, String username, String ipAddress, String userAgent) {
        // 增加失败计数
        user.failedLoginCount = (user.failedLoginCount == null ? 0 : user.failedLoginCount) + 1;

        // 记录失败
        LoginAttempt.fail(username, ipAddress, userAgent,
            String.format("密码错误 (第 %d 次)", user.failedLoginCount));

        // 检查是否需要锁定账户
        if (user.failedLoginCount >= maxFailedAttempts) {
            user.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            Log.warnf("Account locked for user %s until %s", username, user.lockedUntil);
        }
    }

    /**
     * 用户登出
     */
    @Transactional
    public void logout(String username, String refreshToken, boolean allDevices) {
        User user = User.find("username", username).firstResult();
        if (user == null) {
            return;
        }

        if (allDevices) {
            // 撤销所有刷新令牌
            tokenService.revokeAllRefreshTokens(user.id);
            Log.infof("User %s logged out from all devices", username);
        } else if (refreshToken != null) {
            // 只撤销当前刷新令牌
            tokenService.revokeRefreshToken(refreshToken);
            Log.infof("User %s logged out from current device", username);
        }
    }

    /**
     * 刷新访问令牌
     */
    @Transactional
    public AuthRecord.TokenResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        String newToken = tokenService.validateAndRefresh(refreshToken, null, ipAddress, userAgent);

        if (newToken == null) {
            throw new WebApplicationException("无效或过期的刷新令牌", Response.Status.UNAUTHORIZED.getStatusCode());
        }

        // 生成新的刷新令牌
        RefreshToken storedToken = RefreshToken.findByTokenHash(
            tokenService.hashToken(refreshToken)
        );
        String newRefreshToken = null;

        if (storedToken != null && storedToken.isValid()) {
            newRefreshToken = tokenService.generateRefreshToken(
                storedToken.user, null, ipAddress, userAgent
            );
            // 撤销旧的刷新令牌
            storedToken.revoked = true;
        }

        return new AuthRecord.TokenResponse(
            newToken,
            newRefreshToken,
            tokenService.getAccessTokenExpiresIn(),
            "Bearer"
        );
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(String username, AuthRecord.ChangePassword request, String ipAddress, String userAgent) {
        User user = User.find("username", username).firstResult();
        if (user == null) {
            throw new WebApplicationException("用户不存在", Response.Status.NOT_FOUND.getStatusCode());
        }

        // 验证当前密码
        if (!passwordUtil.check(request.currentPassword(), user.password)) {
            LoginAttempt.fail(username, ipAddress, userAgent, "修改密码失败：当前密码错误");
            throw new WebApplicationException("当前密码错误", Response.Status.BAD_REQUEST.getStatusCode());
        }

        // 验证新密码强度
        var strengthResult = passwordStrengthUtil.validate(request.newPassword());
        if (!strengthResult.acceptable()) {
            throw new WebApplicationException("新密码强度不足：" + String.join("; ", strengthResult.hints()),
                Response.Status.BAD_REQUEST.getStatusCode());
        }

        // 不能与当前密码相同
        if (passwordUtil.check(request.newPassword(), user.password)) {
            throw new WebApplicationException("新密码不能与当前密码相同", Response.Status.BAD_REQUEST.getStatusCode());
        }

        // 更新密码
        user.password = passwordUtil.hash(request.newPassword());
        Log.infof("Password changed for user %s", username);

        // 如果需要强制登出所有设备
        if (request.forceLogout() != null && request.forceLogout()) {
            tokenService.revokeAllRefreshTokens(user.id);
            Log.infof("All sessions revoked for user %s after password change", username);
        }
    }

    /**
     * 请求密码重置
     */
    @Transactional
    public void requestPasswordReset(AuthRecord.ResetPasswordRequest request) {
        User user = null;

        if (request.email() != null && !request.email().isBlank()) {
            user = User.find("email", request.email()).firstResult();
        } else if (request.phone() != null && !request.phone().isBlank()) {
            user = User.find("phone", request.phone()).firstResult();
        }

        if (user == null) {
            // 为了安全，不提示用户是否存在
            Log.infof("Password reset requested for non-existent user");
            return;
        }

        // 使之前的重置令牌失效
        PasswordResetToken.invalidateAllByUser(user.id);

        // 创建新的重置令牌
        // TODO: 实际发送邮件/短信
        Log.infof("Password reset token created for user %s", user.username);
    }

    /**
     * 验证密码重置
     */
    @Transactional
    public void verifyPasswordReset(AuthRecord.ResetPasswordVerify request) {
        // TODO: 实现验证码验证和密码重置
        throw new WebApplicationException("功能暂未实现", Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    /**
     * 检查密码强度
     */
    public AuthRecord.PasswordStrengthResponse checkPasswordStrength(String password) {
        var result = passwordStrengthUtil.validate(password);
        return new AuthRecord.PasswordStrengthResponse(
            result.score(), result.level(), result.hints(), result.acceptable()
        );
    }

    /**
     * 获取用户路由
     */
    public List<RouteRecord> getRoutes(String username) {
        User user = User.find("username", username).firstResult();
        if (user == null) {
            return new ArrayList<>();
        }

        Set<Module> modules = new HashSet<>();
        user.roles.forEach(role -> modules.addAll(role.modules));

        return buildRouteTree(modules);
    }

    private List<RouteRecord> buildRouteTree(Set<Module> modules) {
        List<Module> visibleModules = modules.stream()
            .filter(m -> m.type != ModuleType.BUTTON)
            .sorted(Comparator.comparingInt(m -> m.sortOrder != null ? m.sortOrder : 0))
            .collect(Collectors.toList());

        Map<UUID, RouteRecord> dtoMap = new HashMap<>();
        List<RouteRecord> roots = new ArrayList<>();

        for (Module m : visibleModules) {
            RouteRecord dto = new RouteRecord(m.path, m.name, null, new ArrayList<>());
            dtoMap.put(m.id, dto);
        }

        for (Module m : visibleModules) {
            RouteRecord dto = dtoMap.get(m.id);
            if (m.parentId == null) {
                roots.add(dto);
            } else {
                RouteRecord parent = dtoMap.get(m.parentId);
                if (parent != null) {
                    parent.routes().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }
}