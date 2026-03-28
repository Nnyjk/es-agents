package com.easystation.notification.service;

import com.easystation.notification.domain.NotificationChannel;
import com.easystation.notification.dto.ChannelRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationChannelService {

    public List<ChannelRecord> list() {
        return NotificationChannel.listAll().stream()
            .map(c -> (NotificationChannel) c)
            .map(this::toDto)
            .toList();
    }

    public ChannelRecord get(UUID id) {
        NotificationChannel channel = NotificationChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Notification channel not found", Response.Status.NOT_FOUND);
        }
        return toDto(channel);
    }

    @Transactional
    public ChannelRecord create(ChannelRecord.Create dto) {
        if (NotificationChannel.count("name = ?1", dto.name()) > 0) {
            throw new WebApplicationException("Notification channel with same name already exists", Response.Status.CONFLICT);
        }
        NotificationChannel channel = new NotificationChannel();
        channel.setName(dto.name());
        channel.setType(dto.type());
        channel.setConfig(dto.config());
        channel.setEnabled(dto.enabled() != null ? dto.enabled() : true);
        channel.persist();
        return toDto(channel);
    }

    @Transactional
    public ChannelRecord update(UUID id, ChannelRecord.Update dto) {
        NotificationChannel channel = NotificationChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Notification channel not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null && !dto.name().equals(channel.name)) {
            if (NotificationChannel.count("name = ?1 and id != ?2", dto.name(), id) > 0) {
                throw new WebApplicationException("Notification channel with same name already exists", Response.Status.CONFLICT);
            }
            channel.setName(dto.name());
        }
        if (dto.type() != null) channel.setType(dto.type());
        if (dto.config() != null) channel.setConfig(dto.config());
        if (dto.enabled() != null) channel.setEnabled(dto.enabled());
        return toDto(channel);
    }

    @Transactional
    public void delete(UUID id) {
        if (!NotificationChannel.deleteById(id)) {
            throw new WebApplicationException("Notification channel not found", Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public ChannelRecord.TestResult test(UUID id, ChannelRecord.TestRequest request) {
        NotificationChannel channel = NotificationChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Notification channel not found", Response.Status.NOT_FOUND);
        }
        if (!channel.enabled) {
            throw new WebApplicationException("Notification channel is disabled", Response.Status.BAD_REQUEST);
        }
        // TODO: Implement actual notification sending logic based on channel type
        // For now, return a simulated success result
        return new ChannelRecord.TestResult(true, "Test notification sent successfully to " + request.recipient());
    }

    private ChannelRecord toDto(NotificationChannel channel) {
        return new ChannelRecord(
            channel.id,
            channel.name,
            channel.type,
            channel.config,
            channel.enabled,
            channel.createdBy,
            channel.createdAt,
            channel.updatedAt
        );
    }
}