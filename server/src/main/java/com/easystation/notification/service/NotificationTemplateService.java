package com.easystation.notification.service;

import com.easystation.notification.domain.NotificationTemplate;
import com.easystation.notification.dto.TemplateRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationTemplateService {

    public List<TemplateRecord> list() {
        return NotificationTemplate.listAll().stream()
            .map(t -> (NotificationTemplate) t)
            .map(this::toDto)
            .toList();
    }

    public TemplateRecord get(UUID id) {
        NotificationTemplate template = NotificationTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Notification template not found", Response.Status.NOT_FOUND);
        }
        return toDto(template);
    }

    @Transactional
    public TemplateRecord create(TemplateRecord.Create dto) {
        if (NotificationTemplate.count("name = ?1", dto.name()) > 0) {
            throw new WebApplicationException("Notification template with same name already exists", Response.Status.CONFLICT);
        }
        NotificationTemplate template = new NotificationTemplate();
        template.setName(dto.name());
        template.setType(dto.type());
        template.setChannelType(dto.channelType());
        template.setContent(dto.content());
        template.setVariables(dto.variables());
        template.persist();
        return toDto(template);
    }

    @Transactional
    public TemplateRecord update(UUID id, TemplateRecord.Update dto) {
        NotificationTemplate template = NotificationTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Notification template not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null && !dto.name().equals(template.name)) {
            if (NotificationTemplate.count("name = ?1 and id != ?2", dto.name(), id) > 0) {
                throw new WebApplicationException("Notification template with same name already exists", Response.Status.CONFLICT);
            }
            template.setName(dto.name());
        }
        if (dto.type() != null) template.setType(dto.type());
        if (dto.channelType() != null) template.setChannelType(dto.channelType());
        if (dto.content() != null) template.setContent(dto.content());
        if (dto.variables() != null) template.setVariables(dto.variables());
        return toDto(template);
    }

    @Transactional
    public void delete(UUID id) {
        if (!NotificationTemplate.deleteById(id)) {
            throw new WebApplicationException("Notification template not found", Response.Status.NOT_FOUND);
        }
    }

    private TemplateRecord toDto(NotificationTemplate template) {
        return new TemplateRecord(
            template.id,
            template.name,
            template.type,
            template.channelType,
            template.content,
            template.variables,
            template.createdBy,
            template.createdAt
        );
    }
}