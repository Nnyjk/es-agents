import request from "./auth";
import type {
  BaselineTask,
  BaselineTemplate,
  BaselineResult,
  BaselineReport,
  VulnerabilityScanTask,
  Vulnerability,
  VulnerabilityTrend,
  VulnerabilityStats,
  ComplianceSelfCheck,
  ComplianceCheckItem,
  ComplianceGapReport,
  AssessmentDocument,
  AssessmentQuestion,
  RemediationTask,
  AssessmentProgress,
  CreateBaselineTaskRequest,
  UpdateBaselineTaskRequest,
  CreateVulnerabilityScanRequest,
  UpdateVulnerabilityStatusRequest,
  CreateSelfCheckRequest,
  CheckItemRequest,
  GenerateDocumentRequest,
  CreateRemediationTaskRequest,
  PaginatedResponse,
} from "../types/security";

const API_PREFIX = "/api/security";

// ==================== 基线检查 ====================

export const getBaselineTasks = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    status: string;
    targetType: string;
  }>,
): Promise<PaginatedResponse<BaselineTask>> => {
  const response = await request.get(`${API_PREFIX}/baseline/tasks`, {
    params,
  });
  return response.data;
};

export const getBaselineTask = async (
  id: string,
): Promise<BaselineTask> => {
  const response = await request.get(
    `${API_PREFIX}/baseline/tasks/${id}`,
  );
  return response.data;
};

export const createBaselineTask = async (
  data: CreateBaselineTaskRequest,
): Promise<BaselineTask> => {
  const response = await request.post(
    `${API_PREFIX}/baseline/tasks`,
    data,
  );
  return response.data;
};

export const updateBaselineTask = async (
  id: string,
  data: UpdateBaselineTaskRequest,
): Promise<BaselineTask> => {
  const response = await request.put(
    `${API_PREFIX}/baseline/tasks/${id}`,
    data,
  );
  return response.data;
};

export const deleteBaselineTask = async (
  id: string,
): Promise<void> => {
  await request.delete(`${API_PREFIX}/baseline/tasks/${id}`);
};

export const executeBaselineTask = async (
  id: string,
): Promise<BaselineResult> => {
  const response = await request.post(
    `${API_PREFIX}/baseline/tasks/${id}/execute`,
  );
  return response.data;
};

export const getBaselineResult = async (
  taskId: string,
): Promise<BaselineResult> => {
  const response = await request.get(
    `${API_PREFIX}/baseline/tasks/${taskId}/result`,
  );
  return response.data;
};

export const getBaselineTemplates = async (
  params?: Partial<{ category: string; level: string }>,
): Promise<BaselineTemplate[]> => {
  const response = await request.get(
    `${API_PREFIX}/baseline/templates`,
    { params },
  );
  return response.data;
};

export const getBaselineTemplate = async (
  id: string,
): Promise<BaselineTemplate> => {
  const response = await request.get(
    `${API_PREFIX}/baseline/templates/${id}`,
  );
  return response.data;
};

export const generateBaselineReport = async (
  taskId: string,
  format: "pdf" | "word" | "html",
): Promise<BaselineReport> => {
  const response = await request.post(
    `${API_PREFIX}/baseline/tasks/${taskId}/report`,
    { format },
  );
  return response.data;
};

// ==================== 漏洞扫描 ====================

export const getVulnerabilityScanTasks = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    status: string;
    scanType: string;
  }>,
): Promise<PaginatedResponse<VulnerabilityScanTask>> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/tasks`,
    { params },
  );
  return response.data;
};

export const getVulnerabilityScanTask = async (
  id: string,
): Promise<VulnerabilityScanTask> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/tasks/${id}`,
  );
  return response.data;
};

export const createVulnerabilityScanTask = async (
  data: CreateVulnerabilityScanRequest,
): Promise<VulnerabilityScanTask> => {
  const response = await request.post(
    `${API_PREFIX}/vulnerability/tasks`,
    data,
  );
  return response.data;
};

export const updateVulnerabilityScanTask = async (
  id: string,
  data: Partial<CreateVulnerabilityScanRequest>,
): Promise<VulnerabilityScanTask> => {
  const response = await request.put(
    `${API_PREFIX}/vulnerability/tasks/${id}`,
    data,
  );
  return response.data;
};

export const deleteVulnerabilityScanTask = async (
  id: string,
): Promise<void> => {
  await request.delete(`${API_PREFIX}/vulnerability/tasks/${id}`);
};

export const executeVulnerabilityScanTask = async (
  id: string,
): Promise<void> => {
  await request.post(
    `${API_PREFIX}/vulnerability/tasks/${id}/execute`,
  );
};

export const getVulnerabilities = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    severity: string;
    status: string;
    search: string;
  }>,
): Promise<PaginatedResponse<Vulnerability>> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/list`,
    { params },
  );
  return response.data;
};

export const getVulnerability = async (
  id: string,
): Promise<Vulnerability> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/list/${id}`,
  );
  return response.data;
};

export const updateVulnerabilityStatus = async (
  data: UpdateVulnerabilityStatusRequest,
): Promise<Vulnerability> => {
  const response = await request.put(
    `${API_PREFIX}/vulnerability/list/${data.vulnerabilityId}/status`,
    data,
  );
  return response.data;
};

export const getVulnerabilityStats = async (): Promise<VulnerabilityStats> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/stats`,
  );
  return response.data;
};

export const getVulnerabilityTrend = async (
  params?: Partial<{ days: number }>,
): Promise<VulnerabilityTrend[]> => {
  const response = await request.get(
    `${API_PREFIX}/vulnerability/trend`,
    { params },
  );
  return response.data;
};

// ==================== 合规自查 ====================

export const getSelfChecks = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    status: string;
    level: string;
  }>,
): Promise<PaginatedResponse<ComplianceSelfCheck>> => {
  const response = await request.get(
    `${API_PREFIX}/compliance/self-checks`,
    { params },
  );
  return response.data;
};

export const getSelfCheck = async (
  id: string,
): Promise<ComplianceSelfCheck> => {
  const response = await request.get(
    `${API_PREFIX}/compliance/self-checks/${id}`,
  );
  return response.data;
};

export const createSelfCheck = async (
  data: CreateSelfCheckRequest,
): Promise<ComplianceSelfCheck> => {
  const response = await request.post(
    `${API_PREFIX}/compliance/self-checks`,
    data,
  );
  return response.data;
};

export const deleteSelfCheck = async (
  id: string,
): Promise<void> => {
  await request.delete(
    `${API_PREFIX}/compliance/self-checks/${id}`,
  );
};

export const startSelfCheck = async (
  id: string,
): Promise<ComplianceSelfCheck> => {
  const response = await request.post(
    `${API_PREFIX}/compliance/self-checks/${id}/start`,
  );
  return response.data;
};

export const checkComplianceItem = async (
  data: CheckItemRequest,
): Promise<ComplianceCheckItem> => {
  const response = await request.put(
    `${API_PREFIX}/compliance/self-checks/${data.checkId}/items/${data.itemId}`,
    data,
  );
  return response.data;
};

export const generateGapReport = async (
  checkId: string,
): Promise<ComplianceGapReport> => {
  const response = await request.post(
    `${API_PREFIX}/compliance/self-checks/${checkId}/gap-report`,
  );
  return response.data;
};

// ==================== 等保测评 ====================

export const getAssessmentDocuments = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    type: string;
  }>,
): Promise<PaginatedResponse<AssessmentDocument>> => {
  const response = await request.get(
    `${API_PREFIX}/assessment/documents`,
    { params },
  );
  return response.data;
};

export const generateDocument = async (
  data: GenerateDocumentRequest,
): Promise<AssessmentDocument> => {
  const response = await request.post(
    `${API_PREFIX}/assessment/documents/generate`,
    data,
  );
  return response.data;
};

export const getAssessmentQuestions = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    status: string;
    category: string;
  }>,
): Promise<PaginatedResponse<AssessmentQuestion>> => {
  const response = await request.get(
    `${API_PREFIX}/assessment/questions`,
    { params },
  );
  return response.data;
};

export const answerQuestion = async (
  questionId: string,
  answer: string,
  attachments?: string[],
): Promise<AssessmentQuestion> => {
  const response = await request.put(
    `${API_PREFIX}/assessment/questions/${questionId}/answer`,
    { answer, attachments },
  );
  return response.data;
};

export const getRemediationTasks = async (
  params?: Partial<{
    page: number;
    pageSize: number;
    status: string;
    priority: string;
    assignee: string;
  }>,
): Promise<PaginatedResponse<RemediationTask>> => {
  const response = await request.get(
    `${API_PREFIX}/assessment/remediation-tasks`,
    { params },
  );
  return response.data;
};

export const createRemediationTask = async (
  data: CreateRemediationTaskRequest,
): Promise<RemediationTask> => {
  const response = await request.post(
    `${API_PREFIX}/assessment/remediation-tasks`,
    data,
  );
  return response.data;
};

export const updateRemediationTask = async (
  id: string,
  data: Partial<CreateRemediationTaskRequest & { status: string; notes: string }>,
): Promise<RemediationTask> => {
  const response = await request.put(
    `${API_PREFIX}/assessment/remediation-tasks/${id}`,
    data,
  );
  return response.data;
};

export const getAssessmentProgress = async (): Promise<AssessmentProgress> => {
  const response = await request.get(
    `${API_PREFIX}/assessment/progress`,
  );
  return response.data;
};