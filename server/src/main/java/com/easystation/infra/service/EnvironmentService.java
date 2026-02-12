package com.easystation.infra.service;

import com.easystation.infra.domain.Environment;
import com.easystation.infra.domain.Host;
import com.easystation.infra.record.EnvironmentRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EnvironmentService {

    public List<EnvironmentRecord> list() {
        return Environment.listAll().stream()
            .map(e -> (Environment) e)
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public EnvironmentRecord create(EnvironmentRecord.Create dto) {
        if (Environment.count("code = ?1 or name = ?2", dto.code(), dto.name()) > 0) {
            throw new WebApplicationException("Environment with same code or name already exists", Response.Status.CONFLICT);
        }
        Environment env = new Environment();
        env.setName(dto.name());
        env.setCode(dto.code());
        env.setDescription(dto.description());
        env.setColor(dto.color());
        env.setEnabled(true);
        env.persist();
        return toDto(env);
    }

    @Transactional
    public EnvironmentRecord update(UUID id, EnvironmentRecord.Update dto) {
        Environment env = Environment.findById(id);
        if (env == null) {
            throw new WebApplicationException("Environment not found", Response.Status.NOT_FOUND);
        }
        if (dto.description() != null) env.setDescription(dto.description());
        if (dto.enabled() != null) env.setEnabled(dto.enabled());
        if (dto.color() != null) env.setColor(dto.color());
        return toDto(env);
    }

    @Transactional
    public void delete(UUID id) {
        if (Host.count("environment.id", id) > 0) {
            throw new WebApplicationException("Cannot delete environment with associated hosts", Response.Status.CONFLICT);
        }
        if (!Environment.deleteById(id)) {
            throw new WebApplicationException("Environment not found", Response.Status.NOT_FOUND);
        }
    }

    private EnvironmentRecord toDto(Environment env) {
        return new EnvironmentRecord(
            env.id,
            env.name,
            env.code,
            env.description,
            env.enabled,
            env.color,
            env.createdAt,
            env.updatedAt
        );
    }
}
