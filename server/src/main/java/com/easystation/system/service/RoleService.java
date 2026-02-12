package com.easystation.system.service;

import com.easystation.system.domain.Module;
import com.easystation.system.domain.ModuleAction;
import com.easystation.system.domain.Role;
import com.easystation.system.domain.User;
import com.easystation.system.record.RoleRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService {

    public List<RoleRecord> list() {
        return Role.<Role>listAll()
            .stream().map(this::toDto).toList();
    }

    public RoleRecord get(UUID id) {
        Role role = Role.findById(id);
        if (role == null) {
            throw new WebApplicationException("Role not found", 404);
        }
        return toDto(role);
    }

    @Transactional
    public RoleRecord create(RoleRecord dto) {
        if (Role.find("code", dto.code()).firstResult() != null) {
            throw new WebApplicationException("Role code already exists", 400);
        }
        Role role = new Role();
        role.code = dto.code();
        role.name = dto.name();
        role.description = dto.description();
        
        role.persist();
        
        if (dto.moduleIds() != null && !dto.moduleIds().isEmpty()) {
            List<Module> modules = Module.list("id in ?1", dto.moduleIds());
            role.modules = new HashSet<>(modules);
        }

        if (dto.actionIds() != null && !dto.actionIds().isEmpty()) {
            List<ModuleAction> actions = ModuleAction.list("id in ?1", dto.actionIds());
            role.actions = new HashSet<>(actions);
        }
        
        role.persist();
        return toDto(role);
    }

    @Transactional
    public RoleRecord update(UUID id, RoleRecord dto) {
        Role role = Role.findById(id);
        if (role == null) {
            throw new WebApplicationException("Role not found", 404);
        }
        
        if (dto.name() != null) role.name = dto.name();
        if (dto.description() != null) role.description = dto.description();
        
        if (dto.moduleIds() != null) {
            List<Module> modules = Module.list("id in ?1", dto.moduleIds());
            role.modules = new HashSet<>(modules);
        }

        if (dto.actionIds() != null) {
            List<ModuleAction> actions = ModuleAction.list("id in ?1", dto.actionIds());
            role.actions = new HashSet<>(actions);
        }
        
        role.persist();
        return toDto(role);
    }

    @Transactional
    public void delete(UUID id) {
        long count = User.count("select count(u) from User u join u.roles r where r.id = ?1", id);
        if (count > 0) {
            throw new WebApplicationException("Cannot delete role: Assigned to users", 400);
        }
        Role.deleteById(id);
    }

    private RoleRecord toDto(Role role) {
        return new RoleRecord(
            role.id,
            role.code,
            role.name,
            role.description,
            role.modules == null ? null : role.modules.stream().map(m -> m.id).collect(Collectors.toSet()),
            role.actions == null ? null : role.actions.stream().map(a -> a.id).collect(Collectors.toSet())
        );
    }
}
