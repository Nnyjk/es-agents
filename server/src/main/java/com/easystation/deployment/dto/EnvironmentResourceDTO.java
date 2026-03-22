package com.easystation.deployment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class EnvironmentResourceDTO {
    public UUID id;
    public String name;
    public String type;
    public String status;
    public Integer cpuCores;
    public Integer memoryGB;
    public Integer diskGB;
    public String clusterName;
    public String namespace;
}