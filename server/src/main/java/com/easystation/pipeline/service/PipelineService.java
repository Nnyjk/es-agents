package com.easystation.pipeline.service;

import com.easystation.pipeline.domain.Pipeline;
import com.easystation.pipeline.domain.PipelineExecution;
import com.easystation.pipeline.domain.StageExecution;
import com.easystation.pipeline.dto.PipelineRecord;
import com.easystation.pipeline.enums.ExecutionStatus;
import com.easystation.pipeline.enums.PipelineStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PipelineService {

    @Inject
    ObjectMapper objectMapper;

    public List<PipelineRecord.Detail> list(PipelineRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.status() != null) {
            sql.append(" and status = :status");
            params.put("status", query.status());
        }
        if (query.environmentId() != null) {
            sql.append(" and environmentId = :environmentId");
            params.put("environmentId", query.environmentId());
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and name like :keyword");
            params.put("keyword", "%" + query.keyword() + "%");
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return Pipeline.<Pipeline>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public PipelineRecord.Detail get(UUID id) {
        Pipeline pipeline = Pipeline.findById(id);
        if (pipeline == null) {
            throw new WebApplicationException("Pipeline not found", Response.Status.NOT_FOUND);
        }
        return toDetail(pipeline);
    }

    @Transactional
    public PipelineRecord.Detail create(PipelineRecord.Create dto) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = dto.name();
        pipeline.description = dto.description();
        pipeline.environmentId = dto.environmentId();
        pipeline.templateId = dto.templateId();
        pipeline.stages = toJson(dto.stages());
        pipeline.triggerConfig = dto.triggerConfig();
        pipeline.persist();
        return toDetail(pipeline);
    }

    @Transactional
    public PipelineRecord.Detail update(UUID id, PipelineRecord.Update dto) {
        Pipeline pipeline = Pipeline.findById(id);
        if (pipeline == null) {
            throw new WebApplicationException("Pipeline not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null) pipeline.name = dto.name();
        if (dto.description() != null) pipeline.description = dto.description();
        if (dto.status() != null) pipeline.status = dto.status();
        if (dto.environmentId() != null) pipeline.environmentId = dto.environmentId();
        if (dto.templateId() != null) pipeline.templateId = dto.templateId();
        if (dto.stages() != null) pipeline.stages = toJson(dto.stages());
        if (dto.triggerConfig() != null) pipeline.triggerConfig = dto.triggerConfig();
        if (dto.enabled() != null) pipeline.enabled = dto.enabled();
        return toDetail(pipeline);
    }

    @Transactional
    public void delete(UUID id) {
        Pipeline pipeline = Pipeline.findById(id);
        if (pipeline == null) {
            throw new WebApplicationException("Pipeline not found", Response.Status.NOT_FOUND);
        }
        pipeline.delete();
    }

    @Transactional
    public PipelineRecord.ExecutionDetail execute(PipelineRecord.ExecutionCreate dto) {
        Pipeline pipeline = Pipeline.findById(dto.pipelineId());
        if (pipeline == null) {
            throw new WebApplicationException("Pipeline not found", Response.Status.NOT_FOUND);
        }
        if (pipeline.status != PipelineStatus.ACTIVE) {
            throw new WebApplicationException("Pipeline is not active", Response.Status.BAD_REQUEST);
        }

        List<PipelineRecord.StageConfig> stages = parseStages(pipeline.stages);

        PipelineExecution execution = new PipelineExecution();
        execution.pipelineId = dto.pipelineId();
        execution.triggerType = dto.triggerType();
        execution.triggeredBy = dto.triggeredBy();
        execution.version = dto.version();
        execution.totalStages = stages.size();
        execution.status = ExecutionStatus.RUNNING;
        execution.startedAt = LocalDateTime.now();
        execution.persist();

        for (int i = 0; i < stages.size(); i++) {
            PipelineRecord.StageConfig stageConfig = stages.get(i);
            StageExecution stage = new StageExecution();
            stage.executionId = execution.id;
            stage.type = stageConfig.type();
            stage.name = stageConfig.name();
            stage.orderIndex = stageConfig.order();
            stage.config = stageConfig.config();
            stage.persist();
        }

        return toExecutionDetail(execution);
    }

    @Transactional
    public PipelineRecord.ExecutionDetail advanceStage(UUID executionId) {
        PipelineExecution execution = PipelineExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Execution not found", Response.Status.NOT_FOUND);
        }

        List<StageExecution> stages = StageExecution.<StageExecution>find("executionId", executionId)
                .stream()
                .sorted(Comparator.comparingInt(s -> s.orderIndex))
                .collect(Collectors.toList());

        if (execution.currentStage < stages.size()) {
            StageExecution current = stages.get(execution.currentStage);
            current.status = ExecutionStatus.SUCCESS;
            current.finishedAt = LocalDateTime.now();
            if (current.startedAt != null) {
                current.duration = ChronoUnit.MILLIS.between(current.startedAt, current.finishedAt);
            }
            execution.currentStage++;
        }

        if (execution.currentStage >= stages.size()) {
            execution.status = ExecutionStatus.SUCCESS;
            execution.finishedAt = LocalDateTime.now();
            if (execution.startedAt != null) {
                execution.duration = ChronoUnit.MILLIS.between(execution.startedAt, execution.finishedAt);
            }
        }

        return toExecutionDetail(execution);
    }

    @Transactional
    public PipelineRecord.ExecutionDetail cancelExecution(UUID executionId) {
        PipelineExecution execution = PipelineExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Execution not found", Response.Status.NOT_FOUND);
        }
        execution.status = ExecutionStatus.CANCELLED;
        execution.finishedAt = LocalDateTime.now();
        return toExecutionDetail(execution);
    }

    public List<PipelineRecord.ExecutionDetail> listExecutions(PipelineRecord.ExecutionQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.pipelineId() != null) {
            sql.append(" and pipelineId = :pipelineId");
            params.put("pipelineId", query.pipelineId());
        }
        if (query.status() != null) {
            sql.append(" and status = :status");
            params.put("status", query.status());
        }
        if (query.triggerType() != null) {
            sql.append(" and triggerType = :triggerType");
            params.put("triggerType", query.triggerType());
        }
        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return PipelineExecution.<PipelineExecution>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toExecutionDetail)
                .collect(Collectors.toList());
    }

    public PipelineRecord.ExecutionDetail getExecution(UUID id) {
        PipelineExecution execution = PipelineExecution.findById(id);
        if (execution == null) {
            throw new WebApplicationException("Execution not found", Response.Status.NOT_FOUND);
        }
        return toExecutionDetail(execution);
    }

    public List<PipelineRecord.StageDetail> getStages(UUID executionId) {
        return StageExecution.<StageExecution>find("executionId", executionId)
                .stream()
                .sorted(Comparator.comparingInt(s -> s.orderIndex))
                .map(this::toStageDetail)
                .collect(Collectors.toList());
    }

    private PipelineRecord.Detail toDetail(Pipeline pipeline) {
        return new PipelineRecord.Detail(
                pipeline.id,
                pipeline.name,
                pipeline.description,
                pipeline.status,
                pipeline.environmentId,
                pipeline.templateId,
                parseStages(pipeline.stages),
                pipeline.triggerConfig,
                pipeline.enabled,
                pipeline.createdAt,
                pipeline.updatedAt
        );
    }

    private PipelineRecord.ExecutionDetail toExecutionDetail(PipelineExecution execution) {
        return new PipelineRecord.ExecutionDetail(
                execution.id,
                execution.pipelineId,
                execution.status,
                execution.triggerType,
                execution.triggeredBy,
                execution.deploymentId,
                execution.version,
                execution.logs,
                execution.errorMessage,
                execution.currentStage,
                execution.totalStages,
                execution.startedAt,
                execution.finishedAt,
                execution.duration,
                execution.createdAt
        );
    }

    private PipelineRecord.StageDetail toStageDetail(StageExecution stage) {
        return new PipelineRecord.StageDetail(
                stage.id,
                stage.executionId,
                stage.type,
                stage.name,
                stage.orderIndex,
                stage.status,
                stage.config,
                stage.logs,
                stage.errorMessage,
                stage.startedAt,
                stage.finishedAt,
                stage.duration
        );
    }

    private String toJson(List<PipelineRecord.StageConfig> stages) {
        if (stages == null || stages.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(stages);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<PipelineRecord.StageConfig> parseStages(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}