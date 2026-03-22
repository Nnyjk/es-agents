package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentApplication;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ApplicationStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ApplicationService {

    public PageResultDTO<ApplicationDTO> listApplications(int pageNum, int pageSize, String name, String project, String owner, ApplicationStatus status) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" and name like :name");
            params.put("name", "%" + name + "%");
        }
        if (project != null && !project.isEmpty()) {
            queryBuilder.append(" and project = :project");
            params.put("project", project);
        }
        if (owner != null && !owner.isEmpty()) {
            queryBuilder.append(" and owner = :owner");
            params.put("owner", owner);
        }
        if (status != null) {
            queryBuilder.append(" and status = :status");
            params.put("status", status);
        }
        
        long total = DeploymentApplication.count(queryBuilder.toString(), params);
        List<DeploymentApplication> applications = DeploymentApplication.find(queryBuilder.toString(), Sort.by("createdAt").descending(), params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<ApplicationDTO> result = new PageResultDTO<>();
        result.setList(applications.stream().map(this::toDTO).collect(Collectors.toList()));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    public ApplicationDTO getApplication(UUID id) {
        DeploymentApplication app = DeploymentApplication.findById(id);
        if (app == null) {
            throw new IllegalArgumentException("Application not found: " + id);
        }
        return toDTO(app);
    }

    @Transactional
    public ApplicationDTO createApplication(ApplicationDTO dto) {
        DeploymentApplication app = new DeploymentApplication();
        app.name = dto.getName();
        app.project = dto.getProject();
        app.owner = dto.getOwner();
        app.techStack = dto.getTechStack() != null ? String.join(",", dto.getTechStack()) : null;
        app.status = dto.getStatus() != null ? dto.getStatus() : ApplicationStatus.ACTIVE;
        
        if (dto.getConfig() != null) {
            app.repositoryUrl = dto.getConfig().getRepositoryUrl();
            app.branch = dto.getConfig().getBranch();
            app.buildScript = dto.getConfig().getBuildScript();
            app.deployPath = dto.getConfig().getDeployPath();
            app.healthCheckUrl = dto.getConfig().getHealthCheckUrl();
            app.buildCommand = dto.getConfig().getBuildCommand();
            app.startCommand = dto.getConfig().getStartCommand();
            app.stopCommand = dto.getConfig().getStopCommand();
        }
        
        app.persist();
        return toDTO(app);
    }

    @Transactional
    public ApplicationDTO updateApplication(UUID id, ApplicationDTO dto) {
        DeploymentApplication app = DeploymentApplication.findById(id);
        if (app == null) {
            throw new IllegalArgumentException("Application not found: " + id);
        }
        
        if (dto.getName() != null) app.name = dto.getName();
        if (dto.getProject() != null) app.project = dto.getProject();
        if (dto.getOwner() != null) app.owner = dto.getOwner();
        if (dto.getTechStack() != null) app.techStack = String.join(",", dto.getTechStack());
        if (dto.getStatus() != null) app.status = dto.getStatus();
        
        if (dto.getConfig() != null) {
            if (dto.getConfig().getRepositoryUrl() != null) app.repositoryUrl = dto.getConfig().getRepositoryUrl();
            if (dto.getConfig().getBranch() != null) app.branch = dto.getConfig().getBranch();
            if (dto.getConfig().getBuildScript() != null) app.buildScript = dto.getConfig().getBuildScript();
            if (dto.getConfig().getDeployPath() != null) app.deployPath = dto.getConfig().getDeployPath();
            if (dto.getConfig().getHealthCheckUrl() != null) app.healthCheckUrl = dto.getConfig().getHealthCheckUrl();
            if (dto.getConfig().getBuildCommand() != null) app.buildCommand = dto.getConfig().getBuildCommand();
            if (dto.getConfig().getStartCommand() != null) app.startCommand = dto.getConfig().getStartCommand();
            if (dto.getConfig().getStopCommand() != null) app.stopCommand = dto.getConfig().getStopCommand();
        }
        
        app.persist();
        return toDTO(app);
    }

    @Transactional
    public void deleteApplication(UUID id) {
        DeploymentApplication app = DeploymentApplication.findById(id);
        if (app == null) {
            throw new IllegalArgumentException("Application not found: " + id);
        }
        app.delete();
    }

    @Transactional
    public ApplicationDTO archiveApplication(UUID id) {
        DeploymentApplication app = DeploymentApplication.findById(id);
        if (app == null) {
            throw new IllegalArgumentException("Application not found: " + id);
        }
        app.status = ApplicationStatus.ARCHIVED;
        app.persist();
        return toDTO(app);
    }

    private ApplicationDTO toDTO(DeploymentApplication app) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(app.id);
        dto.setName(app.name);
        dto.setProject(app.project);
        dto.setOwner(app.owner);
        dto.setTechStack(app.techStack != null ? Arrays.asList(app.techStack.split(",")) : new ArrayList<>());
        dto.setCurrentVersion(app.currentVersion);
        dto.setStatus(app.status);
        dto.setCreatedAt(app.createdAt);
        dto.setUpdatedAt(app.updatedAt);
        
        ApplicationConfigDTO config = new ApplicationConfigDTO();
        config.setRepositoryUrl(app.repositoryUrl);
        config.setBranch(app.branch);
        config.setBuildScript(app.buildScript);
        config.setDeployPath(app.deployPath);
        config.setHealthCheckUrl(app.healthCheckUrl);
        config.setBuildCommand(app.buildCommand);
        config.setStartCommand(app.startCommand);
        config.setStopCommand(app.stopCommand);
        dto.setConfig(config);
        
        return dto;
    }
}