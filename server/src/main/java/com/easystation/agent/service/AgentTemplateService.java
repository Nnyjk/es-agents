package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentSource;
import com.easystation.agent.domain.AgentTemplate;
import com.easystation.agent.domain.enums.AgentSourceType;
import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.record.AgentCommandRecord;
import com.easystation.agent.record.AgentSourceRecord;
import com.easystation.agent.record.AgentRepositoryRecord;
import com.easystation.agent.record.AgentCredentialRecord;
import com.easystation.agent.domain.AgentRepository;
import com.easystation.agent.domain.AgentCredential;
import com.easystation.agent.record.AgentTemplateRecord;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentTemplateService {

    @Inject
    AgentSourceService agentSourceService;

    public List<AgentTemplateRecord> list(String osType, String sourceType) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (osType != null) {
            query.append(" AND osType = :osType");
            params.put("osType", OsType.valueOf(osType.toUpperCase()));
        }
        if (sourceType != null) {
            query.append(" AND source.type = :sourceType");
            params.put("sourceType", AgentSourceType.valueOf(sourceType.toUpperCase()));
        }

        return AgentTemplate.find(query.toString(), params).stream()
            .map(e -> (AgentTemplate) e)
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public AgentTemplateRecord get(UUID id) {
        AgentTemplate template = AgentTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        return toDto(template);
    }

    @Transactional
    public AgentTemplateRecord create(AgentTemplateRecord.Create dto) {
        AgentSource source = AgentSource.findById(dto.sourceId());
        if (source == null) {
            throw new WebApplicationException("Agent Source not found", Response.Status.BAD_REQUEST);
        }

        AgentTemplate template = new AgentTemplate();
        template.setName(dto.name());
        template.setDescription(dto.description());
        if (dto.osType() != null) {
            template.setOsType(OsType.valueOf(dto.osType()));
        }
        template.setSource(source);
        
        if (dto.commands() != null) {
            template.setCommands(dto.commands().stream().map(cmdDto -> {
                AgentCommand cmd = new AgentCommand();
                cmd.setName(cmdDto.name());
                cmd.setScript(cmdDto.script());
                cmd.setTimeout(cmdDto.timeout());
                cmd.setDefaultArgs(cmdDto.defaultArgs());
                cmd.setTemplate(template);
                return cmd;
            }).collect(Collectors.toList()));
        }
        
        template.persist();
        return toDto(template);
    }

    @Transactional
    public AgentTemplateRecord update(UUID id, AgentTemplateRecord.Update dto) {
        AgentTemplate template = AgentTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        
        if (dto.name() != null) template.setName(dto.name());
        if (dto.description() != null) template.setDescription(dto.description());
        if (dto.osType() != null) template.setOsType(OsType.valueOf(dto.osType()));
        
        if (dto.sourceId() != null) {
            AgentSource source = AgentSource.findById(dto.sourceId());
            if (source == null) {
                throw new WebApplicationException("Agent Source not found", Response.Status.BAD_REQUEST);
            }
            template.setSource(source);
        }
        
        return toDto(template);
    }

    @Transactional
    public void delete(UUID id) {
        if (!AgentTemplate.deleteById(id)) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Download agent package for a template.
     * Delegates to AgentSourceService for unified download handling.
     * 
     * @param templateId Template ID
     * @param fileNameOut Output array to receive the file name (length 1)
     * @return InputStream of the agent package
     */
    public InputStream download(UUID templateId, String[] fileNameOut) {
        AgentTemplate template = AgentTemplate.findById(templateId);
        if (template == null) {
            throw new WebApplicationException("Agent Template not found", Response.Status.NOT_FOUND);
        }
        if (template.source == null) {
            throw new WebApplicationException("Template has no source configured", Response.Status.BAD_REQUEST);
        }
        // Delegate to AgentSourceService for unified download handling
        return agentSourceService.getSourceStream(template.source.id, fileNameOut);
    }

    private AgentTemplateRecord toDto(AgentTemplate template) {
        return new AgentTemplateRecord(
            template.id,
            template.name,
            template.description,
            template.osType != null ? template.osType.name() : null,
            template.source != null ? new AgentSourceRecord(
                template.source.id,
                template.source.name,
                template.source.type,
                template.source.config,
                toRepositorySimpleDto(template.source.repository),
                toCredentialSimpleDto(template.source.credential),
                template.source.createdAt,
                template.source.updatedAt
            ) : null,
            template.commands != null ? template.commands.stream().map(this::toCommandDto).collect(Collectors.toList()) : List.of(),
            template.createdAt,
            template.updatedAt
        );
    }

    private AgentCommandRecord toCommandDto(AgentCommand cmd) {
        return new AgentCommandRecord(
            cmd.id,
            cmd.name,
            cmd.script,
            cmd.timeout,
            cmd.defaultArgs,
            cmd.template != null ? cmd.template.id : null
        );
    }

    private AgentRepositoryRecord.Simple toRepositorySimpleDto(AgentRepository repository) {
        if (repository == null) {
            return null;
        }
        return new AgentRepositoryRecord.Simple(
            repository.id,
            repository.name,
            repository.type
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
