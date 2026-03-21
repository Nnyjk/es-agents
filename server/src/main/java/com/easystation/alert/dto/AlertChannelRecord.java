package com.easystation.alert.dto;

import com.easystation.alert.enums.AlertChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class AlertChannelRecord {

    public record Create(
            @NotBlank String name,
            @NotNull AlertChannelType type,
            String config,
            List<String> receivers,
            Boolean enabled
    ) {}

    public record Update(
            String name,
            AlertChannelType type,
            String config,
            List<String> receivers,
            Boolean enabled
    ) {}

    public record Detail(
            UUID id,
            String name,
            AlertChannelType type,
            String config,
            List<String> receivers,
            boolean enabled,
            String createdAt,
            String updatedAt
    ) {}
}