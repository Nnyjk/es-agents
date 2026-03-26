package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentEnvironment;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.EnvironmentType;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class EnvironmentService {

    public PageResultDTO<EnvironmentDTO> listEnvironments(int pageNum, int pageSize, String name, 
                                                          EnvironmentType environmentType, Boolean active) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" and name like :name");
            params.put("name", "%" + name + "%");
        }
        if (environmentType != null) {
            queryBuilder.append(" and environmentType = :environmentType");
            params.put("environmentType", environmentType);
        }
        if (active != null) {
            queryBuilder.append(" and active = :active");
            params.put("active", active);
        }
        
        long total = DeploymentEnvironment.count(queryBuilder.toString(), params);
        List<DeploymentEnvironment> environments = DeploymentEnvironment.find(queryBuilder.toString(), Sort.by("createdAt").descending(), params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<EnvironmentDTO> result = new PageResultDTO<>();
        result.setData(environments.stream().map(this::toDTO).collect(Collectors.toList()));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    public EnvironmentDTO getEnvironment(UUID id) {
        DeploymentEnvironment env = DeploymentEnvironment.findById(id);
        if (env == null) {
            throw new IllegalArgumentException("Environment not found: " + id);
        }
        return toDTO(env);
    }

    @Transactional
    public EnvironmentDTO createEnvironment(EnvironmentDTO dto) {
        DeploymentEnvironment env = new DeploymentEnvironment();
        env.name = dto.getName();
        env.environmentType = dto.getEnvironmentType() != null ? dto.getEnvironmentType() : EnvironmentType.DEV;
        env.description = dto.getDescription();
        env.clusterConfig = dto.getClusterConfig();
        env.resourceQuota = dto.getResourceQuota();
        env.permissions = dto.getPermissions() != null ? String.join(",", dto.getPermissions()) : null;
        env.configCenter = dto.getConfigCenter();
        env.active = dto.getActive() != null ? dto.getActive() : true;
        env.createdBy = dto.getCreatedBy();
        
        env.persist();
        return toDTO(env);
    }

    @Transactional
    public EnvironmentDTO updateEnvironment(UUID id, EnvironmentDTO dto) {
        DeploymentEnvironment env = DeploymentEnvironment.findById(id);
        if (env == null) {
            throw new IllegalArgumentException("Environment not found: " + id);
        }
        
        if (dto.getName() != null) env.name = dto.getName();
        if (dto.getEnvironmentType() != null) env.environmentType = dto.getEnvironmentType();
        if (dto.getDescription() != null) env.description = dto.getDescription();
        if (dto.getClusterConfig() != null) env.clusterConfig = dto.getClusterConfig();
        if (dto.getResourceQuota() != null) env.resourceQuota = dto.getResourceQuota();
        if (dto.getPermissions() != null) env.permissions = String.join(",", dto.getPermissions());
        if (dto.getConfigCenter() != null) env.configCenter = dto.getConfigCenter();
        if (dto.getActive() != null) env.active = dto.getActive();
        
        env.persist();
        return toDTO(env);
    }

    @Transactional
    public void deleteEnvironment(UUID id) {
        DeploymentEnvironment env = DeploymentEnvironment.findById(id);
        if (env == null) {
            throw new IllegalArgumentException("Environment not found: " + id);
        }
        env.delete();
    }

    public List<EnvironmentResourceDTO> getEnvironmentResources(UUID environmentId) {
        // Placeholder - in production, this would query actual infrastructure
        List<EnvironmentResourceDTO> resources = new ArrayList<>();
        
        EnvironmentResourceDTO resource1 = new EnvironmentResourceDTO();
        resource1.setId(UUID.randomUUID());
        resource1.setName("node-1");
        resource1.setType("node");
        resource1.setStatus("running");
        resource1.setCpuCores(8);
        resource1.setMemoryGB(32);
        resource1.setDiskGB(500);
        resource1.setClusterName("cluster-1");
        resources.add(resource1);
        
        EnvironmentResourceDTO resource2 = new EnvironmentResourceDTO();
        resource2.setId(UUID.randomUUID());
        resource2.setName("node-2");
        resource2.setType("node");
        resource2.setStatus("running");
        resource2.setCpuCores(8);
        resource2.setMemoryGB(32);
        resource2.setDiskGB(500);
        resource2.setClusterName("cluster-1");
        resources.add(resource2);
        
        return resources;
    }

    public List<EnvironmentApplicationDTO> getEnvironmentApplications(UUID environmentId) {
        // Placeholder - in production, this would query deployed applications
        return new ArrayList<>();
    }

    private EnvironmentDTO toDTO(DeploymentEnvironment env) {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setId(env.id);
        dto.setName(env.name);
        dto.setEnvironmentType(env.environmentType);
        dto.setDescription(env.description);
        dto.setClusterConfig(env.clusterConfig);
        dto.setResourceQuota(env.resourceQuota);
        dto.setPermissions(env.permissions != null ? Arrays.asList(env.permissions.split(",")) : new ArrayList<>());
        dto.setConfigCenter(env.configCenter);
        dto.setActive(env.active);
        dto.setCreatedBy(env.createdBy);
        dto.setCreatedAt(env.createdAt);
        dto.setUpdatedAt(env.updatedAt);
        
        return dto;
    }
}