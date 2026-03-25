package com.easystation.deployment.dto;

import com.easystation.deployment.enums.RollbackStrategy;
import lombok.Data;

import java.util.UUID;

/**
 * 回滚请求 DTO
 */
@Data
public class RollbackRequestDTO {
    public UUID targetVersionId;
    public RollbackStrategy strategy = RollbackStrategy.FAST;
    public String reason;
    public Integer timeout = 300;
    public Integer maxRetry = 3;
    public Boolean skipVerify = false;
    public String notifyConfig;
}