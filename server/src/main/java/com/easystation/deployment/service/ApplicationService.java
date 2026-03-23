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
        
        if (dto.getCodeConfig() != null) {
            app.repositoryUrl = dto.getCodeConfig().getRepositoryUrl();
            app.branch = dto.getCodeConfig().getBranch();
            app.buildScript = dto.getCodeConfig().getBuildScript();
            app.deployPath = dto.getCodeConfig().getDeployPath();
            app.healthCheckUrl = dto.getCodeConfig().getHealthCheckUrl();
            app.buildCommand = dto.getCodeConfig().getBuildCommand();
            app.startCommand = dto.getCodeConfig().getStartCommand();
            app.stopCommand = dto.getCodeConfig().getStopCommand();
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
        
        if (dto.getCodeConfig() != null) {
            if (dto.getCodeConfig().getRepositoryUrl() != null) app.repositoryUrl = dto.getCodeConfig().getRepositoryUrl();
            if (dto.getCodeConfig().getBranch() != null) app.branch = dto.getCodeConfig().getBranch();
            if (dto.getCodeConfig().getBuildScript() != null) app.buildScript = dto.getCodeConfig().getBuildScript();
            if (dto.getCodeConfig().getDeployPath() != null) app.deployPath = dto.getCodeConfig().getDeployPath();
            if (dto.getCodeConfig().getHealthCheckUrl() != null) app.healthCheckUrl = dto.getCodeConfig().getHealthCheckUrl();
            if (dto.getCodeConfig().getBuildCommand() != null) app.buildCommand = dto.getCodeConfig().getBuildCommand();
            if (dto.getCodeConfig().getStartCommand() != null) app.startCommand = dto.getCodeConfig().getStartCommand();
            if (dto.getCodeConfig().getStopCommand() != null) app.stopCommand = dto.getCodeConfig().getStopCommand();
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
        
        ApplicationCodeConfigDTO codeConfig = new ApplicationCodeConfigDTO();
        codeConfig.setRepositoryUrl(app.repositoryUrl);
        codeConfig.setBranch(app.branch);
        codeConfig.setBuildScript(app.buildScript);
        codeConfig.setDeployPath(app.deployPath);
        codeConfig.setHealthCheckUrl(app.healthCheckUrl);
        codeConfig.setBuildCommand(app.buildCommand);
        codeConfig.setStartCommand(app.startCommand);
        codeConfig.setStopCommand(app.stopCommand);
        dto.setCodeConfig(codeConfig);
        
        return dto;
    }
}