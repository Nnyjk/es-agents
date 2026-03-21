package com.easystation.alert.service;

import com.easystation.alert.domain.AlertChannel;
import com.easystation.alert.dto.AlertChannelRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AlertChannelService {

    @Inject
    ObjectMapper objectMapper;

    public List<AlertChannelRecord.Detail> list() {
        return AlertChannel.<AlertChannel>listAll().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AlertChannelRecord.Detail get(UUID id) {
        AlertChannel channel = AlertChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Alert channel not found", Response.Status.NOT_FOUND);
        }
        return toDetail(channel);
    }

    @Transactional
    public AlertChannelRecord.Detail create(AlertChannelRecord.Create dto) {
        AlertChannel channel = new AlertChannel();
        channel.name = dto.name();
        channel.type = dto.type();
        channel.config = dto.config();
        channel.receivers = toJson(dto.receivers());
        if (dto.enabled() != null) {
            channel.enabled = dto.enabled();
        }
        channel.persist();
        return toDetail(channel);
    }

    @Transactional
    public AlertChannelRecord.Detail update(UUID id, AlertChannelRecord.Update dto) {
        AlertChannel channel = AlertChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Alert channel not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null) channel.name = dto.name();
        if (dto.type() != null) channel.type = dto.type();
        if (dto.config() != null) channel.config = dto.config();
        if (dto.receivers() != null) channel.receivers = toJson(dto.receivers());
        if (dto.enabled() != null) channel.enabled = dto.enabled();
        return toDetail(channel);
    }

    @Transactional
    public void delete(UUID id) {
        AlertChannel channel = AlertChannel.findById(id);
        if (channel == null) {
            throw new WebApplicationException("Alert channel not found", Response.Status.NOT_FOUND);
        }
        channel.delete();
    }

    private AlertChannelRecord.Detail toDetail(AlertChannel channel) {
        return new AlertChannelRecord.Detail(
                channel.id,
                channel.name,
                channel.type,
                channel.config,
                fromJson(channel.receivers),
                channel.enabled,
                channel.createdAt != null ? channel.createdAt.toString() : null,
                channel.updatedAt != null ? channel.updatedAt.toString() : null
        );
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}