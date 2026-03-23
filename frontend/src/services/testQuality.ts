/**
 * 测试与质量管控管理 API 服务
 */

import axios from 'axios';
import type {
  TestCase,
  TestSuite,
  TestPlan,
  TestCaseExecution,
  TestTask,
  TaskExecution,
  TestReport,
  Defect,
  QualityMetrics,
  QualityTrend,
  QualityGate,
  QualityReport,
  Pipeline,
  PipelineConfig,
  ReleaseVersion,
  PageParams,
  PageResult,
  ApiResponse,
  ReportTrend,
  TestEnvironment,
  GlobalConfig,
  NotificationConfig,
  ExecutionStatistics,
} from '../types/testQuality';

const API_BASE = '/api/test-quality';

// ============ 测试用例管理 API ============

/** 获取测试用例列表 */
export async function getTestCases(
  params: PageParams & {
    keyword?: string;
    type?: string;
    priority?: string;
    status?: string;
    module?: string;
  }
): Promise<PageResult<TestCase>> {
  const response = await axios.get<PageResult<TestCase>>(`${API_BASE}/test-cases`, { params });
  return response.data;
}

/** 获取测试用例详情 */
export async function getTestCase(id: string): Promise<TestCase> {
  const response = await axios.get<ApiResponse<TestCase>>(`${API_BASE}/test-cases/${id}`);
  return response.data.data!;
}

/** 创建测试用例 */
export async function createTestCase(data: Partial<TestCase>): Promise<TestCase> {
  const response = await axios.post<ApiResponse<TestCase>>(`${API_BASE}/test-cases`, data);
  return response.data.data!;
}

/** 更新测试用例 */
export async function updateTestCase(id: string, data: Partial<TestCase>): Promise<TestCase> {
  const response = await axios.put<ApiResponse<TestCase>>(`${API_BASE}/test-cases/${id}`, data);
  return response.data.data!;
}

/** 删除测试用例 */
export async function deleteTestCase(id: string): Promise<void> {
  await axios.delete(`${API_BASE}/test-cases/${id}`);
}

/** 导入测试用例 */
export async function importTestCases(file: File): Promise<{ success: number; failed: number }> {
  const formData = new FormData();
  formData.append('file', file);
  const response = await axios.post<ApiResponse<{ success: number; failed: number }>>(
    `${API_BASE}/test-cases/import`,
    formData
  );
  return response.data.data!;
}

/** 导出测试用例 */
export async function exportTestCases(ids: string[], format: 'excel' | 'json'): Promise<Blob> {
  const response = await axios.post(
    `${API_BASE}/test-cases/export`,
    { ids, format },
    { responseType: 'blob' }
  );
  return response.data;
}

/** 获取测试套件列表 */
export async function getTestSuites(params: PageParams): Promise<PageResult<TestSuite>> {
  const response = await axios.get<PageResult<TestSuite>>(`${API_BASE}/test-suites`, { params });
  return response.data;
}

/** 创建测试套件 */
export async function createTestSuite(data: Partial<TestSuite>): Promise<TestSuite> {
  const response = await axios.post<ApiResponse<TestSuite>>(`${API_BASE}/test-suites`, data);
  return response.data.data!;
}

/** 获取测试计划列表 */
export async function getTestPlans(params: PageParams): Promise<PageResult<TestPlan>> {
  const response = await axios.get<PageResult<TestPlan>>(`${API_BASE}/test-plans`, { params });
  return response.data;
}

/** 获取用例执行记录 */
export async function getCaseExecutions(
  params: PageParams & { testCaseId?: string; testPlanId?: string; status?: string }
): Promise<PageResult<TestCaseExecution>> {
  const response = await axios.get<PageResult<TestCaseExecution>>(
    `${API_BASE}/case-executions`,
    { params }
  );
  return response.data;
}

// ============ 自动化测试任务管理 API ============

/** 获取测试任务列表 */
export async function getTestTasks(
  params: PageParams & { status?: string; environment?: string }
): Promise<PageResult<TestTask>> {
  const response = await axios.get<PageResult<TestTask>>(`${API_BASE}/test-tasks`, { params });
  return response.data;
}

/** 获取测试任务详情 */
export async function getTestTask(id: string): Promise<TestTask> {
  const response = await axios.get<ApiResponse<TestTask>>(`${API_BASE}/test-tasks/${id}`);
  return response.data.data!;
}

/** 创建测试任务 */
export async function createTestTask(data: Partial<TestTask>): Promise<TestTask> {
  const response = await axios.post<ApiResponse<TestTask>>(`${API_BASE}/test-tasks`, data);
  return response.data.data!;
}

/** 更新测试任务 */
export async function updateTestTask(id: string, data: Partial<TestTask>): Promise<TestTask> {
  const response = await axios.put<ApiResponse<TestTask>>(`${API_BASE}/test-tasks/${id}`, data);
  return response.data.data!;
}

/** 删除测试任务 */
export async function deleteTestTask(id: string): Promise<void> {
  await axios.delete(`${API_BASE}/test-tasks/${id}`);
}

/** 执行测试任务 */
export async function executeTestTask(id: string): Promise<TaskExecution> {
  const response = await axios.post<ApiResponse<TaskExecution>>(
    `${API_BASE}/test-tasks/${id}/execute`
  );
  return response.data.data!;
}

/** 获取任务执行详情 */
export async function getTaskExecution(id: string): Promise<TaskExecution> {
  const response = await axios.get<ApiResponse<TaskExecution>>(
    `${API_BASE}/task-executions/${id}`
  );
  return response.data.data!;
}

/** 暂停任务执行 */
export async function pauseTaskExecution(id: string): Promise<void> {
  await axios.post(`${API_BASE}/task-executions/${id}/pause`);
}

/** 取消任务执行 */
export async function cancelTaskExecution(id: string): Promise<void> {
  await axios.post(`${API_BASE}/task-executions/${id}/cancel`);
}

/** 重跑失败用例 */
export async function retryFailedCases(executionId: string): Promise<TaskExecution> {
  const response = await axios.post<ApiResponse<TaskExecution>>(
    `${API_BASE}/task-executions/${executionId}/retry-failed`
  );
  return response.data.data!;
}

/** 批量执行任务 */
export async function batchExecuteTasks(ids: string[]): Promise<{ success: number; failed: number }> {
  const response = await axios.post<ApiResponse<{ success: number; failed: number }>>(
    `${API_BASE}/test-tasks/batch-execute`,
    { ids }
  );
  return response.data.data!;
}

// ============ 测试结果与缺陷管理 API ============

/** 获取测试报告列表 */
export async function getTestReports(
  params: PageParams & { testPlanId?: string; status?: string }
): Promise<PageResult<TestReport>> {
  const response = await axios.get<PageResult<TestReport>>(`${API_BASE}/test-reports`, { params });
  return response.data;
}

/** 获取测试报告详情 */
export async function getTestReport(id: string): Promise<TestReport> {
  const response = await axios.get<ApiResponse<TestReport>>(`${API_BASE}/test-reports/${id}`);
  return response.data.data!;
}

/** 获取测试报告详情（别名，用于页面调用） */
export async function getTestReportDetail(id: string): Promise<TestReport> {
  return getTestReport(id);
}

/** 获取报告趋势数据 */
export async function getReportTrends(): Promise<ReportTrend[]> {
  const response = await axios.get<ApiResponse<ReportTrend[]>>(`${API_BASE}/test-reports/trends`);
  return response.data.data || [];
}

/** 导出测试报告 */
export async function exportReport(id: string, format: string): Promise<void> {
  await axios.post(`${API_BASE}/test-reports/${id}/export`, { format });
}

/** 导出测试报告（别名） */
export async function exportTestReport(id: string, format: 'html' | 'pdf' | 'excel'): Promise<Blob> {
  const response = await axios.get(`${API_BASE}/test-reports/${id}/export`, {
    params: { format },
    responseType: 'blob',
  });
  return response.data;
}

/** 获取缺陷列表 */
export async function getDefects(
  params: PageParams & {
    keyword?: string;
    severity?: string;
    status?: string;
    assignee?: string;
  }
): Promise<PageResult<Defect>> {
  const response = await axios.get<PageResult<Defect>>(`${API_BASE}/defects`, { params });
  return response.data;
}

/** 获取缺陷详情 */
export async function getDefect(id: string): Promise<Defect> {
  const response = await axios.get<ApiResponse<Defect>>(`${API_BASE}/defects/${id}`);
  return response.data.data!;
}

/** 创建缺陷 */
export async function createDefect(data: Partial<Defect>): Promise<Defect> {
  const response = await axios.post<ApiResponse<Defect>>(`${API_BASE}/defects`, data);
  return response.data.data!;
}

/** 更新缺陷 */
export async function updateDefect(id: string, data: Partial<Defect>): Promise<Defect> {
  const response = await axios.put<ApiResponse<Defect>>(`${API_BASE}/defects/${id}`, data);
  return response.data.data!;
}

/** 获取缺陷统计 */
export async function getDefectStats(): Promise<{
  total: number;
  byStatus: Record<string, number>;
  bySeverity: Record<string, number>;
}> {
  const response = await axios.get<ApiResponse<{
    total: number;
    byStatus: Record<string, number>;
    bySeverity: Record<string, number>;
  }>>(`${API_BASE}/defects/stats`);
  return response.data.data!;
}

// ============ 质量度量与分析 API ============

/** 获取质量指标 */
export async function getQualityMetrics(): Promise<QualityMetrics> {
  const response = await axios.get<ApiResponse<QualityMetrics>>(`${API_BASE}/quality/metrics`);
  return response.data.data!;
}

/** 获取质量趋势 */
export async function getQualityTrend(
  params: { startDate: string; endDate: string; project?: string }
): Promise<QualityTrend[]> {
  const response = await axios.get<QualityTrend[]>(`${API_BASE}/quality/trend`, { params });
  return response.data;
}

/** 获取质量门禁列表 */
export async function getQualityGates(): Promise<QualityGate[]> {
  const response = await axios.get<ApiResponse<QualityGate[]>>(`${API_BASE}/quality-gates`);
  return response.data.data!;
}

/** 创建质量门禁 */
export async function createQualityGate(data: Partial<QualityGate>): Promise<QualityGate> {
  const response = await axios.post<ApiResponse<QualityGate>>(`${API_BASE}/quality-gates`, data);
  return response.data.data!;
}

/** 更新质量门禁 */
export async function updateQualityGate(id: string, data: Partial<QualityGate>): Promise<QualityGate> {
  const response = await axios.put<ApiResponse<QualityGate>>(`${API_BASE}/quality-gates/${id}`, data);
  return response.data.data!;
}

/** 删除质量门禁 */
export async function deleteQualityGate(id: string): Promise<void> {
  await axios.delete(`${API_BASE}/quality-gates/${id}`);
}

/** 获取质量报表列表 */
export async function getQualityReports(params: PageParams): Promise<PageResult<QualityReport>> {
  const response = await axios.get<PageResult<QualityReport>>(`${API_BASE}/quality/reports`, {
    params,
  });
  return response.data;
}

/** 生成质量报表 */
export async function generateQualityReport(
  type: 'daily' | 'weekly' | 'monthly' | 'release',
  params: { project?: string; version?: string }
): Promise<QualityReport> {
  const response = await axios.post<ApiResponse<QualityReport>>(
    `${API_BASE}/quality/reports/generate`,
    { type, ...params }
  );
  return response.data.data!;
}

// ============ CI/CD流水线集成 API ============

/** 获取流水线列表 */
export async function getPipelines(
  params: PageParams & { project?: string; status?: string; environment?: string }
): Promise<PageResult<Pipeline>> {
  const response = await axios.get<PageResult<Pipeline>>(`${API_BASE}/pipelines`, { params });
  return response.data;
}

/** 获取流水线详情 */
export async function getPipeline(id: string): Promise<Pipeline> {
  const response = await axios.get<ApiResponse<Pipeline>>(`${API_BASE}/pipelines/${id}`);
  return response.data.data!;
}

/** 触发流水线 */
export async function triggerPipeline(id: string): Promise<Pipeline> {
  const response = await axios.post<ApiResponse<Pipeline>>(`${API_BASE}/pipelines/${id}/trigger`);
  return response.data.data!;
}

/** 取消流水线 */
export async function cancelPipeline(id: string): Promise<void> {
  await axios.post(`${API_BASE}/pipelines/${id}/cancel`);
}

/** 重试流水线 */
export async function retryPipeline(id: string): Promise<Pipeline> {
  const response = await axios.post<ApiResponse<Pipeline>>(`${API_BASE}/pipelines/${id}/retry`);
  return response.data.data!;
}

/** 获取流水线配置列表 */
export async function getPipelineConfigs(params: PageParams): Promise<PageResult<PipelineConfig>> {
  const response = await axios.get<PageResult<PipelineConfig>>(`${API_BASE}/pipeline-configs`, {
    params,
  });
  return response.data;
}

/** 创建流水线配置 */
export async function createPipelineConfig(data: Partial<PipelineConfig>): Promise<PipelineConfig> {
  const response = await axios.post<ApiResponse<PipelineConfig>>(
    `${API_BASE}/pipeline-configs`,
    data
  );
  return response.data.data!;
}

/** 更新流水线配置 */
export async function updatePipelineConfig(
  id: string,
  data: Partial<PipelineConfig>
): Promise<PipelineConfig> {
  const response = await axios.put<ApiResponse<PipelineConfig>>(
    `${API_BASE}/pipeline-configs/${id}`,
    data
  );
  return response.data.data!;
}

/** 删除流水线配置 */
export async function deletePipelineConfig(id: string): Promise<void> {
  await axios.delete(`${API_BASE}/pipeline-configs/${id}`);
}

/** 获取发布版本列表 */
export async function getReleaseVersions(
  params: PageParams & { project?: string }
): Promise<PageResult<ReleaseVersion>> {
  const response = await axios.get<PageResult<ReleaseVersion>>(`${API_BASE}/release-versions`, {
    params,
  });
  return response.data;
}

/** 获取发布版本详情 */
export async function getReleaseVersion(id: string): Promise<ReleaseVersion> {
  const response = await axios.get<ApiResponse<ReleaseVersion>>(`${API_BASE}/release-versions/${id}`);
  return response.data.data!;
}

// ============ 测试配置管理 API ============

/** 获取测试环境列表 */
export async function getTestEnvironments(): Promise<TestEnvironment[]> {
  const response = await axios.get<ApiResponse<TestEnvironment[]>>(`${API_BASE}/environments`);
  return response.data.data || [];
}

/** 创建测试环境 */
export async function createTestEnvironment(data: Partial<TestEnvironment>): Promise<TestEnvironment> {
  const response = await axios.post<ApiResponse<TestEnvironment>>(`${API_BASE}/environments`, data);
  return response.data.data!;
}

/** 更新测试环境 */
export async function updateTestEnvironment(id: string, data: Partial<TestEnvironment>): Promise<TestEnvironment> {
  const response = await axios.put<ApiResponse<TestEnvironment>>(`${API_BASE}/environments/${id}`, data);
  return response.data.data!;
}

/** 删除测试环境 */
export async function deleteTestEnvironment(id: string): Promise<void> {
  await axios.delete(`${API_BASE}/environments/${id}`);
}

/** 测试环境连接 */
export async function testConnection(id: string): Promise<{ success: boolean; message: string }> {
  const response = await axios.post<ApiResponse<{ success: boolean; message: string }>>(
    `${API_BASE}/environments/${id}/test-connection`
  );
  return response.data.data!;
}

/** 获取全局配置 */
export async function getGlobalConfig(): Promise<GlobalConfig> {
  const response = await axios.get<ApiResponse<GlobalConfig>>(`${API_BASE}/config/global`);
  return response.data.data!;
}

/** 更新全局配置 */
export async function updateGlobalConfig(data: Partial<GlobalConfig>): Promise<GlobalConfig> {
  const response = await axios.put<ApiResponse<GlobalConfig>>(`${API_BASE}/config/global`, data);
  return response.data.data!;
}

/** 获取通知配置 */
export async function getNotificationConfig(): Promise<NotificationConfig> {
  const response = await axios.get<ApiResponse<NotificationConfig>>(`${API_BASE}/config/notification`);
  return response.data.data!;
}

/** 更新通知配置 */
export async function updateNotificationConfig(data: Partial<NotificationConfig>): Promise<NotificationConfig> {
  const response = await axios.put<ApiResponse<NotificationConfig>>(`${API_BASE}/config/notification`, data);
  return response.data.data!;
}

/** 获取执行统计数据 */
export async function getExecutionStatistics(): Promise<ExecutionStatistics> {
  const response = await axios.get<ApiResponse<ExecutionStatistics>>(`${API_BASE}/executions/statistics`);
  return response.data.data!;
}