package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentApplication;
import com.easystation.deployment.domain.DeploymentPipeline;
import com.easystation.deployment.domain.PipelineExecution;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.PipelineStatus;
import com.easystation.deployment.enums.PipelineType;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PipelineService {

    public PageResultDTO<PipelineDTO> listPipelines(int pageNum, int pageSize, String name, UUID applicationId, PipelineStatus status) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" and name like :name");
            params.put("name", "%" + name + "%");
        }
        if (applicationId != null) {
            queryBuilder.append(" and applicationId = :applicationId");
            params.put("applicationId", applicationId);
        }
        if (status != null) {
            queryBuilder.append(" and status = :status");
            params.put("status", status);
        }
        
        long total = DeploymentPipeline.count(queryBuilder.toString(), params);
        List<DeploymentPipeline> pipelines = DeploymentPipeline.find(queryBuilder.toString(), Sort.by("createdAt").descending(), params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<PipelineDTO> result = new PageResultDTO<>();
        result.setList(pipelines.stream().map(this::toDTO).collect(Collectors.toList()));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    public PipelineDTO getPipeline(UUID id) {
        DeploymentPipeline pipeline = DeploymentPipeline.findById(id);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + id);
        }
        return toDTO(pipeline);
    }

    @Transactional
    public PipelineDTO createPipeline(PipelineDTO dto) {
        DeploymentPipeline pipeline = new DeploymentPipeline();
        pipeline.name = dto.getName();
        pipeline.applicationId = dto.getApplicationId();
        pipeline.pipelineType = dto.getPipelineType() != null ? dto.getPipelineType() : PipelineType.BUILD_DEPLOY;
        pipeline.description = dto.getDescription();
        pipeline.stages = dto.getStages() != null ? serializeStages(dto.getStages()) : null;
        pipeline.status = PipelineStatus.PENDING;
        pipeline.triggerType = dto.getTriggerType() != null ? dto.getTriggerType() : "manual";
        pipeline.cronExpression = dto.getCronExpression();
        pipeline.createdBy = dto.getCreatedBy();
        
        pipeline.persist();
        return toDTO(pipeline);
    }

    @Transactional
    public PipelineDTO updatePipeline(UUID id, PipelineDTO dto) {
        DeploymentPipeline pipeline = DeploymentPipeline.findById(id);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + id);
        }
        
        if (dto.getName() != null) pipeline.name = dto.getName();
        if (dto.getPipelineType() != null) pipeline.pipelineType = dto.getPipelineType();
        if (dto.getDescription() != null) pipeline.description = dto.getDescription();
        if (dto.getStages() != null) pipeline.stages = serializeStages(dto.getStages());
        if (dto.getTriggerType() != null) pipeline.triggerType = dto.getTriggerType();
        if (dto.getCronExpression() != null) pipeline.cronExpression = dto.getCronExpression();
        
        pipeline.persist();
        return toDTO(pipeline);
    }

    @Transactional
    public void deletePipeline(UUID id) {
        DeploymentPipeline pipeline = DeploymentPipeline.findById(id);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + id);
        }
        pipeline.delete();
    }

    @Transactional
    public PipelineExecutionDTO triggerPipeline(UUID id, String triggeredBy) {
        DeploymentPipeline pipeline = DeploymentPipeline.findById(id);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + id);
        }
        
        // Create execution record
        PipelineExecution execution = new PipelineExecution();
        execution.pipelineId = id;
        execution.executionNumber = getNextExecutionNumber(id);
        execution.status = PipelineStatus.RUNNING;
        execution.triggeredBy = triggeredBy;
        execution.triggerType = "manual";
        execution.startedAt = java.time.LocalDateTime.now();
        execution.persist();
        
        // Update pipeline status
        pipeline.status = PipelineStatus.RUNNING;
        pipeline.lastExecutionAt = java.time.LocalDateTime.now();
        pipeline.persist();
        
        return toExecutionDTO(execution, pipeline.name);
    }

    @Transactional
    public PipelineExecutionDTO cancelPipelineExecution(UUID executionId) {
        PipelineExecution execution = PipelineExecution.findById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }
        
        execution.status = PipelineStatus.CANCELLED;
        execution.finishedAt = java.time.LocalDateTime.now();
        execution.persist();
        
        // Update pipeline status
        DeploymentPipeline pipeline = DeploymentPipeline.findById(execution.pipelineId);
        if (pipeline != null) {
            pipeline.status = PipelineStatus.CANCELLED;
            pipeline.persist();
        }
        
        return toExecutionDTO(execution, pipeline != null ? pipeline.name : null);
    }

    @Transactional
    public PipelineExecutionDTO retryPipelineExecution(UUID executionId) {
        PipelineExecution execution = PipelineExecution.findById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }
        
        return triggerPipeline(execution.pipelineId, execution.triggeredBy);
    }

    public List<PipelineExecutionDTO> getPipelineExecutions(UUID pipelineId) {
        List<PipelineExecution> executions = PipelineExecution.find("pipelineId", Sort.by("createdAt").descending(), pipelineId).list();
        DeploymentPipeline pipeline = DeploymentPipeline.findById(pipelineId);
        String pipelineName = pipeline != null ? pipeline.name : null;
        return executions.stream().map(e -> toExecutionDTO(e, pipelineName)).collect(Collectors.toList());
    }

    public PipelineExecutionDTO getExecutionDetail(UUID executionId) {
        PipelineExecution execution = PipelineExecution.findById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }
        DeploymentPipeline pipeline = DeploymentPipeline.findById(execution.pipelineId);
        return toExecutionDTO(execution, pipeline != null ? pipeline.name : null);
    }

    private Integer getNextExecutionNumber(UUID pipelineId) {
        Long count = PipelineExecution.count("pipelineId", pipelineId);
        return count.intValue() + 1;
    }

    private String serializeStages(List<PipelineStageDTO> stages) {
        if (stages == null || stages.isEmpty()) return null;
        // Simple JSON serialization placeholder
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < stages.size(); i++) {
            PipelineStageDTO stage = stages.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(stage.getName()).append("\"");
            sb.append(",\"type\":\"").append(stage.getType()).append("\"");
            sb.append(",\"order\":").append(stage.getOrder()).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<PipelineStageDTO> deserializeStages(String stages) {
        // Placeholder - in production use Jackson
        return new ArrayList<>();
    }

    private PipelineDTO toDTO(DeploymentPipeline pipeline) {
        PipelineDTO dto = new PipelineDTO();
        dto.setId(pipeline.id);
        dto.setName(pipeline.name);
        dto.setApplicationId(pipeline.applicationId);
        
        // Get application name
        DeploymentApplication app = DeploymentApplication.findById(pipeline.applicationId);
        dto.setApplicationName(app != null ? app.name : null);
        
        dto.setPipelineType(pipeline.pipelineType);
        dto.setDescription(pipeline.description);
        dto.setStages(pipeline.stages != null ? deserializeStages(pipeline.stages) : new ArrayList<>());
        dto.setStatus(pipeline.status);
        dto.setTriggerType(pipeline.triggerType);
        dto.setCronExpression(pipeline.cronExpression);
        dto.setLastExecutionAt(pipeline.lastExecutionAt);
        dto.setLastExecutionStatus(pipeline.lastExecutionStatus);
        dto.setCreatedBy(pipeline.createdBy);
        dto.setCreatedAt(pipeline.createdAt);
        dto.setUpdatedAt(pipeline.updatedAt);
        
        return dto;
    }

    private PipelineExecutionDTO toExecutionDTO(PipelineExecution execution, String pipelineName) {
        PipelineExecutionDTO dto = new PipelineExecutionDTO();
        dto.setId(execution.id);
        dto.setPipelineId(execution.pipelineId);
        dto.setPipelineName(pipelineName);
        dto.setExecutionNumber(execution.executionNumber);
        dto.setStatus(execution.status);
        dto.setTriggeredBy(execution.triggeredBy);
        dto.setTriggerType(execution.triggerType);
        dto.setStartedAt(execution.startedAt);
        dto.setFinishedAt(execution.finishedAt);
        dto.setLogs(execution.logs);
        dto.setStages(new ArrayList<>());
        dto.setCreatedAt(execution.createdAt);
        
        return dto;
    }
}