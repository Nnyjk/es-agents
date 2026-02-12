package com.easystation.system.service;

import com.easystation.system.domain.Role;
import com.easystation.system.domain.User;
import com.easystation.system.domain.enums.UserStatus;
import com.easystation.system.record.UserRecord;
import com.easystation.common.utils.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordUtil passwordUtil;

    public List<User> list() {
        return User.listAll();
    }

    @Transactional
    public User create(UserRecord.Create dto) {
        if (User.find("username", dto.username()).firstResult() != null) {
            throw new WebApplicationException("Username already exists", 400);
        }
        User user = new User();
        user.username = dto.username();
        user.password = passwordUtil.hash(dto.password());
        user.status = UserStatus.ACTIVE;

        if (dto.roleIds() != null && !dto.roleIds().isEmpty()) {
            List<Role> roles = Role.list("id in ?1", dto.roleIds());
            user.roles = new HashSet<>(roles);
        }

        user.persist();
        return user;
    }

    @Transactional
    public User update(UUID id, UserRecord.Update dto) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        
        if (dto.status() != null) {
            user.status = dto.status();
        }
        if (dto.roleIds() != null) {
            List<Role> roles = Role.list("id in ?1", dto.roleIds());
            user.roles = new HashSet<>(roles);
        }
        
        user.persist();
        return user;
    }

    @Transactional
    public void delete(UUID id) {
        if (!User.deleteById(id)) {
            throw new WebApplicationException("User not found", 404);
        }
    }

    @Transactional
    public User changeStatus(UUID id, UserStatus status) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        user.status = status;
        user.persist();
        return user;
    }
}
