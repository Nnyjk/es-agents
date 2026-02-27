package com.easystation.auth.service;

import com.easystation.auth.record.LoginResponse;
import com.easystation.auth.record.RouteRecord;
import com.easystation.common.utils.PasswordUtil;
import com.easystation.system.domain.Module;
import com.easystation.system.domain.User;
import com.easystation.system.domain.enums.ModuleType;
import com.easystation.system.domain.enums.UserStatus;
import com.easystation.system.record.RoleRecord;
import com.easystation.system.record.UserRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordUtil passwordUtil;
    @Inject
    TokenService tokenService;

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

    @Transactional
    public LoginResponse login(UserRecord.Login request) {
        User user = User.find("username", request.username()).firstResult();

        if (user == null) {
             throw new WebApplicationException("Invalid credentials", 401);
        }

        // 暂时剔除登录加密功能，直接使用明文密码校验
        String passwordToCheck = request.password();

        if (!passwordUtil.check(passwordToCheck, user.password)) {
            throw new WebApplicationException("Invalid credentials", 401);
        }
        if (user.status != UserStatus.ACTIVE) {
            throw new WebApplicationException("User account is " + user.status, 403);
        }

        Set<String> roleCodes = user.roles.stream().map(r -> r.code).collect(Collectors.toSet());
        String token = tokenService.generateToken(user.username, roleCodes);

        Set<RoleRecord> roleDtos = user.roles.stream().map(r -> new RoleRecord(
            r.id,
            r.code,
            r.name,
            r.description,
            null,
            null
        )).collect(Collectors.toSet());

        UserRecord userDto = new UserRecord(
            user.id,
            user.username,
            user.status,
            roleDtos
        );
        
        Set<String> menus = new HashSet<>();
        Set<String> actions = new HashSet<>();
        user.roles.forEach(role -> {
            role.modules.forEach(m -> menus.add(m.code));
            role.actions.forEach(a -> actions.add(a.code));
        });

        LoginResponse.PermissionRecord permDto = new LoginResponse.PermissionRecord(menus, actions);
        return new LoginResponse(token, userDto, permDto);
    }
}
