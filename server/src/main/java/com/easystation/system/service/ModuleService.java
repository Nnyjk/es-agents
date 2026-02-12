package com.easystation.system.service;

import com.easystation.system.domain.Module;
import com.easystation.system.domain.ModuleAction;
import com.easystation.system.domain.Role;
import com.easystation.system.record.ModuleActionRecord;
import com.easystation.system.record.ModuleRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ModuleService {

    public List<Module> list() {
        return Module.list("order by sortOrder");
    }

    @Transactional
    public Module create(ModuleRecord dto) {
        Module module = new Module();
        module.code = dto.code();
        module.name = dto.name();
        module.type = dto.type();
        module.path = dto.path();
        module.parentId = dto.parentId();
        module.sortOrder = dto.sortOrder();
        module.persist();
        return module;
    }

    @Transactional
    public Module update(UUID id, ModuleRecord dto) {
        Module module = Module.findById(id);
        if (module == null) {
            throw new WebApplicationException("Module not found", 404);
        }
        module.name = dto.name();
        module.path = dto.path();
        module.sortOrder = dto.sortOrder();
        module.persist();
        return module;
    }

    @Transactional
    public void delete(UUID id) {
        long roleCount = Role.count("select count(r) from Role r join r.modules m where m.id = ?1", id);
        if (roleCount > 0) {
            throw new WebApplicationException("Cannot delete module: Used by roles", 400);
        }
        long childCount = Module.count("parentId", id);
        if (childCount > 0) {
            throw new WebApplicationException("Cannot delete module: Has children", 400);
        }
        Module.deleteById(id);
    }

    // Module Actions
    @Transactional
    public ModuleAction createAction(ModuleActionRecord dto) {
        Module module = Module.findById(dto.moduleId());
        if (module == null) {
            throw new WebApplicationException("Module not found", 404);
        }
        ModuleAction action = new ModuleAction();
        action.module = module;
        action.code = dto.code();
        action.name = dto.name();
        action.persist();
        return action;
    }

    @Transactional
    public void deleteAction(UUID id) {
         long count = Role.count("select count(r) from Role r join r.actions a where a.id = ?1", id);
         if (count > 0) {
             throw new WebApplicationException("Cannot delete action: Used by roles", 400);
         }
         ModuleAction.deleteById(id);
    }
}
