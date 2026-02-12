package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCredential;
import com.easystation.agent.domain.AgentRepository;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.record.AgentCredentialRecord;
import com.easystation.agent.record.AgentRepositoryRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AgentRepositoryService {

    public List<AgentRepositoryRecord> list() {
        return AgentRepository.listAll().stream()
            .map(e -> (AgentRepository) e)
            .map(this::toDto)
            .toList();
    }

    public AgentRepositoryRecord get(UUID id) {
        AgentRepository repository = AgentRepository.findById(id);
        if (repository == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return toDto(repository);
    }

    @Transactional
    public AgentRepositoryRecord create(AgentRepositoryRecord.Create dto) {
        AgentRepository repository = new AgentRepository();
        repository.setName(dto.name());
        repository.setType(dto.type());
        repository.setBaseUrl(dto.baseUrl());
        repository.setProjectPath(dto.projectPath());
        repository.setDefaultBranch(dto.defaultBranch());
        repository.setCredential(resolveCredential(dto.credentialId()));
        repository.persist();
        return toDto(repository);
    }

    @Transactional
    public AgentRepositoryRecord update(UUID id, AgentRepositoryRecord.Update dto) {
        AgentRepository repository = AgentRepository.findById(id);
        if (repository == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null) repository.setName(dto.name());
        if (dto.type() != null) repository.setType(dto.type());
        if (dto.baseUrl() != null) repository.setBaseUrl(dto.baseUrl());
        if (dto.projectPath() != null) repository.setProjectPath(dto.projectPath());
        if (dto.defaultBranch() != null) repository.setDefaultBranch(dto.defaultBranch());
        if (dto.credentialId() != null) repository.setCredential(resolveCredential(dto.credentialId()));
        return toDto(repository);
    }

    @Transactional
    public void delete(UUID id) {
        if (AgentSource.count("repository.id", id) > 0) {
            throw new WebApplicationException("Repository is in use", Response.Status.CONFLICT);
        }
        if (!AgentRepository.deleteById(id)) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
    }

    private AgentCredential resolveCredential(UUID id) {
        if (id == null) {
            return null;
        }
        AgentCredential credential = AgentCredential.findById(id);
        if (credential == null) {
            throw new WebApplicationException("Credential not found", Response.Status.BAD_REQUEST);
        }
        return credential;
    }

    private AgentRepositoryRecord toDto(AgentRepository repository) {
        return new AgentRepositoryRecord(
            repository.id,
            repository.name,
            repository.type,
            repository.baseUrl,
            repository.projectPath,
            repository.defaultBranch,
            toCredentialSimpleDto(repository.credential),
            repository.createdAt,
            repository.updatedAt
        );
    }

    private AgentCredentialRecord.Simple toCredentialSimpleDto(AgentCredential credential) {
        if (credential == null) {
            return null;
        }
        return new AgentCredentialRecord.Simple(
            credential.id,
            credential.name,
            credential.type
        );
    }
}
