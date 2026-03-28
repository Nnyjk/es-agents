package com.easystation.agent.domain.enums;

/**
 * Status for batch operations
 */
public enum BatchOperationStatus {
    PENDING,
    RUNNING,
    PARTIAL_SUCCESS,
    SUCCESS,
    FAILED
}