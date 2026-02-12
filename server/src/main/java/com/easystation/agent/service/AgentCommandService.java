package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.record.AgentCommandRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentCommandService {

    public List<AgentCommandRecord> list(UUID templateId) {
        List<AgentCommand> list;
        if (templateId != null) {
            list = AgentCommand.list("template.id", templateId);
        } else {
            list = AgentCommand.listAll();
        }

        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AgentCommandRecord get(UUID id) {
        AgentCommand cmd = AgentCommand.findById(id);
        if (cmd == null) {
            throw new WebApplicationException("Command not found", Response.Status.NOT_FOUND);
        }
        return toDto(cmd);
    }

    @Transactional
    public AgentCommandRecord create(AgentCommandRecord.Create dto) {
        AgentTemplate template = null;
        if (dto.templateId() != null) {
            template = AgentTemplate.findById(dto.templateId());
            if (template == null) {
                throw new WebApplicationException("Agent Template not found", Response.Status.BAD_REQUEST);
            }
        }

        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.BAD_REQUEST);
        }

        AgentCommand cmd = new AgentCommand();
        cmd.setName(dto.name());
        cmd.setScript(dto.script());
        cmd.setTimeout(dto.timeout() != null ? dto.timeout() : 60L);
        cmd.setDefaultArgs(dto.defaultArgs());
        if (template != null) cmd.setTemplate(template);
        
        cmd.persist();
        return toDto(cmd);
    }

    @Transactional
    public AgentCommandRecord update(UUID id, AgentCommandRecord.Update dto) {
        AgentCommand cmd = AgentCommand.findById(id);
        if (cmd == null) {
            throw new WebApplicationException("Command not found", Response.Status.NOT_FOUND);
        }
        
        if (dto.name() != null) cmd.setName(dto.name());
        if (dto.script() != null) cmd.setScript(dto.script());
        if (dto.timeout() != null) cmd.setTimeout(dto.timeout());
        if (dto.defaultArgs() != null) cmd.setDefaultArgs(dto.defaultArgs());
        
        return toDto(cmd);
    }

    @Transactional
    public void delete(UUID id) {
        if (!AgentCommand.deleteById(id)) {
            throw new WebApplicationException("Command not found", Response.Status.NOT_FOUND);
        }
    }

    private AgentCommandRecord toDto(AgentCommand cmd) {
        return new AgentCommandRecord(
                cmd.id,
                cmd.name,
                cmd.script,
                cmd.timeout,
                cmd.defaultArgs,
                cmd.template != null ? cmd.template.id : null
        );
    }
}
