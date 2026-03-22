package com.easystation.deployment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PipelineStageDTO {
    public String name;
    public String type;
    public Integer order;
    public List<PipelineStepDTO> steps;
    public String config;
}

@Data
class PipelineStepDTO {
    public String name;
    public String type;
    public String command;
    public Integer timeout;
    public Boolean continueOnError;
}