/**
 * 部署与发布管理 API 服务
 */
import request from "../utils/request";
import type {
  Application,
  ApplicationQueryParams,
  ApplicationEnvironment,
  Pipeline,
  PipelineQueryParams,
  PipelineExecution,
  Release,
  ReleaseQueryParams,
  Environment,
  EnvironmentQueryParams,
  EnvironmentResource,
  EnvironmentApplication,
  PageResult,
  DeploymentHistory,
  DeploymentHistoryDetail,
  DeploymentHistoryQueryParams,
  DeploymentStatistics,
} from "@/types/deployment";

// ============== 应用管理 API ==============

/**
 * 获取应用列表
 */
export async function getApplications(
  params: ApplicationQueryParams,
): Promise<PageResult<Application>> {
  return request.get("/api/deployment/applications", { params });
}

/**
 * 获取应用详情
 */
export async function getApplication(id: string): Promise<Application> {
  return request.get(`/api/deployment/applications/${id}`);
}

/**
 * 创建应用
 */
export async function createApplication(
  data: Omit<
    Application,
    "id" | "createdAt" | "updatedAt" | "currentVersion" | "environments"
  >,
): Promise<Application> {
  return request.post("/api/deployment/applications", data);
}

/**
 * 更新应用
 */
export async function updateApplication(
  id: string,
  data: Partial<Application>,
): Promise<Application> {
  return request.put(`/api/deployment/applications/${id}`, data);
}

/**
 * 删除应用
 */
export async function deleteApplication(id: string): Promise<void> {
  return request.delete(`/api/deployment/applications/${id}`);
}

/**
 * 归档应用
 */
export async function archiveApplication(id: string): Promise<Application> {
  return request.post(`/api/deployment/applications/${id}/archive`);
}

/**
 * 获取应用环境状态
 */
export async function getApplicationEnvironments(
  id: string,
): Promise<ApplicationEnvironment[]> {
  return request.get(`/api/deployment/applications/${id}/environments`);
}

// ============== 流水线管理 API ==============

/**
 * 获取流水线列表
 */
export async function getPipelines(
  params: PipelineQueryParams,
): Promise<PageResult<Pipeline>> {
  return request.get("/api/deployment/pipelines", { params });
}

/**
 * 获取流水线详情
 */
export async function getPipeline(id: string): Promise<Pipeline> {
  return request.get(`/api/deployment/pipelines/${id}`);
}

/**
 * 创建流水线
 */
export async function createPipeline(
  data: Omit<
    Pipeline,
    | "id"
    | "createdAt"
    | "updatedAt"
    | "status"
    | "lastExecutionAt"
    | "applicationName"
  >,
): Promise<Pipeline> {
  return request.post("/api/deployment/pipelines", data);
}

/**
 * 更新流水线
 */
export async function updatePipeline(
  id: string,
  data: Partial<Pipeline>,
): Promise<Pipeline> {
  return request.put(`/api/deployment/pipelines/${id}`, data);
}

/**
 * 删除流水线
 */
export async function deletePipeline(id: string): Promise<void> {
  return request.delete(`/api/deployment/pipelines/${id}`);
}

/**
 * 触发流水线
 */
export async function triggerPipeline(id: string): Promise<PipelineExecution> {
  return request.post(`/api/deployment/pipelines/${id}/trigger`);
}

/**
 * 取消流水线执行
 */
export async function cancelPipeline(id: string): Promise<void> {
  return request.post(`/api/deployment/pipelines/${id}/cancel`);
}

/**
 * 重试流水线
 */
export async function retryPipeline(id: string): Promise<PipelineExecution> {
  return request.post(`/api/deployment/pipelines/${id}/retry`);
}

/**
 * 获取流水线执行历史
 */
export async function getPipelineExecutions(
  pipelineId: string,
  params: { current: number; pageSize: number },
): Promise<PageResult<PipelineExecution>> {
  return request.get(`/api/deployment/pipelines/${pipelineId}/executions`, {
    params,
  });
}

/**
 * 获取执行详情
 */
export async function getExecutionDetail(
  executionId: string,
): Promise<PipelineExecution> {
  return request.get(`/api/deployment/executions/${executionId}`);
}

// ============== 发布管理 API ==============

/**
 * 获取发布列表
 */
export async function getReleases(
  params: ReleaseQueryParams,
): Promise<PageResult<Release>> {
  return request.get("/api/deployment/releases", { params });
}

/**
 * 获取发布详情
 */
export async function getReleaseDetail(id: string): Promise<Release> {
  return request.get(`/api/deployment/releases/${id}`);
}

/**
 * 创建发布
 */
export async function createRelease(
  data: Omit<
    Release,
    | "id"
    | "releaseId"
    | "applicationName"
    | "environmentName"
    | "status"
    | "createdAt"
    | "updatedAt"
  >,
): Promise<Release> {
  return request.post("/api/deployment/releases", data);
}

/**
 * 审批发布
 */
export async function approveRelease(id: string): Promise<Release> {
  return request.post(`/api/deployment/releases/${id}/approve`);
}

/**
 * 拒绝发布
 */
export async function rejectRelease(id: string): Promise<Release> {
  return request.post(`/api/deployment/releases/${id}/reject`);
}

/**
 * 开始发布
 */
export async function startRelease(id: string): Promise<Release> {
  return request.post(`/api/deployment/releases/${id}/start`);
}

/**
 * 回滚发布
 */
export async function rollbackRelease(id: string): Promise<Release> {
  return request.post(`/api/deployment/releases/${id}/rollback`);
}

/**
 * 获取发布历史
 */
export async function getReleaseHistory(
  applicationId: string,
): Promise<Release[]> {
  return request.get(`/api/deployment/releases/history`, {
    params: { applicationId },
  });
}

// ============== 环境管理 API ==============

/**
 * 获取环境列表
 */
export async function getEnvironments(
  params: EnvironmentQueryParams,
): Promise<PageResult<Environment>> {
  return request.get("/api/deployment/environments", { params });
}

/**
 * 获取环境详情
 */
export async function getEnvironment(id: string): Promise<Environment> {
  return request.get(`/api/deployment/environments/${id}`);
}

/**
 * 创建环境
 */
export async function createEnvironment(
  data: Omit<
    Environment,
    | "id"
    | "createdAt"
    | "updatedAt"
    | "applicationCount"
    | "resourceUsage"
    | "healthStatus"
  >,
): Promise<Environment> {
  return request.post("/api/deployment/environments", data);
}

/**
 * 更新环境
 */
export async function updateEnvironment(
  id: string,
  data: Partial<Environment>,
): Promise<Environment> {
  return request.put(`/api/deployment/environments/${id}`, data);
}

/**
 * 删除环境
 */
export async function deleteEnvironment(id: string): Promise<void> {
  return request.delete(`/api/deployment/environments/${id}`);
}

/**
 * 获取环境资源使用情况
 */
export async function getEnvironmentResources(
  id: string,
): Promise<EnvironmentResource> {
  return request.get(`/api/deployment/environments/${id}/resources`);
}

/**
 * 获取环境应用列表
 */
export async function getEnvironmentApplications(
  id: string,
): Promise<EnvironmentApplication[]> {
  return request.get(`/api/deployment/environments/${id}/applications`);
}

// ============== 部署历史 API ==============

/**
 * 获取部署历史列表
 */
export async function getDeploymentHistory(
  params: DeploymentHistoryQueryParams,
): Promise<{ list: DeploymentHistory[]; total: number }> {
  const result: { data: DeploymentHistory[]; total: number } =
    await request.get("/api/deployments/history", { params });
  // 后端返回 data 字段，转换为前端 list 字段
  return {
    list: result.data || [],
    total: result.total || 0,
  };
}

/**
 * 获取部署历史详情
 */
export async function getDeploymentHistoryDetail(
  id: string,
): Promise<DeploymentHistoryDetail> {
  return request.get(`/api/deployments/history/${id}`);
}

/**
 * 获取部署统计
 */
export async function getDeploymentStatistics(params?: {
  applicationId?: string;
  environmentId?: string;
  startTime?: string;
  endTime?: string;
}): Promise<DeploymentStatistics> {
  return request.get("/api/deployments/history/statistics", { params });
}

/**
 * 回滚部署
 */
export async function rollbackDeployment(
  releaseId: string,
  reason?: string,
): Promise<Release> {
  return request.post(`/api/deployment/releases/${releaseId}/rollback`, null, {
    params: { rolledBackBy: reason || "system" },
  });
}
