package com.easystation.deployment.dto;

import lombok.Data;

@Data
public class ApplicationConfigDTO {
    public String repositoryUrl;
    public String branch;
    public String buildScript;
    public String deployPath;
    public String healthCheckUrl;
    public String buildCommand;
    public String startCommand;
    public String stopCommand;
}