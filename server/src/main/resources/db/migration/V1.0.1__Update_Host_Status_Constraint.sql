-- V1.0.1 Update Host Status Constraint
-- Fix: Add EXCEPTION to allowed status values in infra_host table

ALTER TABLE infra_host DROP CONSTRAINT IF EXISTS infra_host_status_check;

ALTER TABLE infra_host ADD CONSTRAINT infra_host_status_check 
    CHECK (status IN ('UNCONNECTED', 'ONLINE', 'OFFLINE', 'EXCEPTION'));
