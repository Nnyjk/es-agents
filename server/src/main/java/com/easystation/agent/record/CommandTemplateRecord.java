package com.easystation.agent.record;

import com.easystation.agent.domain.CommandTemplate;
import com.easystation.agent.domain.enums.CommandCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandTemplateRecord {

    public record ListResponse(
            UUID id,
            String name,
            String description,
            CommandCategory category,
            String tags,
            Long timeout,
            Boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ListResponse from(CommandTemplate template) {
            return new ListResponse(
                    template.id,
                    template.name,
                    template.description,
                    template.category,
                    template.tags,
                    template.timeout,
                    template.isActive,
                    template.createdAt,
                    template.updatedAt
            );
        }
    }

    public record DetailResponse(
            UUID id,
            String name,
            String description,
            String script,
            CommandCategory category,
            String tags,
            String parameters,
            Long timeout,
            Integer retryCount,
            Boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy
    ) {
        public static DetailResponse from(CommandTemplate template) {
            return new DetailResponse(
                    template.id,
                    template.name,
                    template.description,
                    template.script,
                    template.category,
                    template.tags,
                    template.parameters,
                    template.timeout,
                    template.retryCount,
                    template.isActive,
                    template.createdAt,
                    template.updatedAt,
                    template.createdBy
            );
        }
    }

    public record CreateRequest(
            String name,
            String description,
            String script,
            CommandCategory category,
            String tags,
            String parameters,
            Long timeout,
            Integer retryCount
    ) {}

    public record UpdateRequest(
            String name,
            String description,
            String script,
            CommandCategory category,
            String tags,
            String parameters,
            Long timeout,
            Integer retryCount,
            Boolean isActive
    ) {}

    public record ExecuteRequest(
            UUID agentInstanceId,
            String parameters
    ) {}

    public record ExecuteResponse(
            UUID executionId,
            String message
    ) {}
}