package com.easystation.deployment.dto;

import com.easystation.deployment.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ApplicationDTO {
    public UUID id;
    public String name;
    public String project;
    public String owner;
    public List<String> techStack;
    public String currentVersion;
    public ApplicationStatus status;
    public ApplicationCodeConfigDTO codeConfig;
    public List<ApplicationEnvironmentDTO> environments;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}