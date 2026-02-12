package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCredential;
import com.easystation.agent.domain.AgentRepository;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.record.AgentCredentialRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AgentCredentialService {

    public List<AgentCredentialRecord> list() {
        return AgentCredential.listAll().stream()
            .map(e -> (AgentCredential) e)
            .map(this::toDto)
            .toList();
    }

    public AgentCredentialRecord get(UUID id) {
        AgentCredential credential = AgentCredential.findById(id);
        if (credential == null) {
            throw new WebApplicationException("Credential not found", Response.Status.NOT_FOUND);
        }
        return toDto(credential);
    }

    @Transactional
    public AgentCredentialRecord create(AgentCredentialRecord.Create dto) {
        AgentCredential credential = new AgentCredential();
        credential.setName(dto.name());
        credential.setType(dto.type());
        credential.setConfig(dto.config());
        credential.persist();
        return toDto(credential);
    }

    @Transactional
    public AgentCredentialRecord update(UUID id, AgentCredentialRecord.Update dto) {
        AgentCredential credential = AgentCredential.findById(id);
        if (credential == null) {
            throw new WebApplicationException("Credential not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null) credential.setName(dto.name());
        if (dto.type() != null) credential.setType(dto.type());
        if (dto.config() != null) credential.setConfig(dto.config());
        return toDto(credential);
    }

    @Transactional
    public void delete(UUID id) {
        if (AgentRepository.count("credential.id", id) > 0 || AgentSource.count("credential.id", id) > 0) {
            throw new WebApplicationException("Credential is in use", Response.Status.CONFLICT);
        }
        if (!AgentCredential.deleteById(id)) {
            throw new WebApplicationException("Credential not found", Response.Status.NOT_FOUND);
        }
    }

    private AgentCredentialRecord toDto(AgentCredential credential) {
        return new AgentCredentialRecord(
            credential.id,
            credential.name,
            credential.type,
            credential.config,
            credential.createdAt,
            credential.updatedAt
        );
    }
}
