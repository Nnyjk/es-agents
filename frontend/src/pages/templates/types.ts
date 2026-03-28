import type { AgentTemplate, Host } from "../../types";

/**
 * Wizard step enumeration
 */
export enum WizardStep {
  SELECT_TEMPLATE = 0,
  CONFIG_INSTANCE = 1,
  BIND_HOSTS = 2,
  PREVIEW = 3,
  RESULT = 4,
}

/**
 * Instance configuration for wizard
 */
export interface InstanceConfig {
  name: string;
  environmentId?: string;
  config: Record<string, any>;
}

/**
 * Created instance result record
 */
export interface CreatedInstance {
  instanceId: string;
  name: string;
  hostId: string;
  hostName: string;
  status: "success" | "failed";
  error?: string;
}

/**
 * Template wizard state
 */
export interface TemplateWizardState {
  currentStep: WizardStep;
  selectedTemplate: AgentTemplate | null;
  instanceConfig: InstanceConfig;
  selectedHosts: Host[];
  createdInstances: CreatedInstance[];
}

/**
 * Batch create request payload
 */
export interface BatchCreateRequest {
  templateId: string;
  hostIds: string[];
  name?: string;
  environmentId?: string;
  config?: Record<string, any>;
}

/**
 * Batch create response
 */
export interface BatchCreateResponse {
  success: boolean;
  results: CreatedInstance[];
  total: number;
  successCount: number;
  failedCount: number;
}
