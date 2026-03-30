import request from "../utils/request";

// ========== Types ==========

export type DiagnosticCategory =
  | "SYSTEM"
  | "PERFORMANCE"
  | "SECURITY"
  | "BUSINESS"
  | "ALERT";
export type FindingSeverity = "INFO" | "WARNING" | "CRITICAL" | "FATAL";
export type ReportStatus = "GENERATING" | "COMPLETED" | "FAILED";

export interface DiagnosticRule {
  ruleId: string;
  name: string;
  description?: string;
  category: DiagnosticCategory;
  condition: string;
  severity: FindingSeverity;
  recommendation?: string;
  enabled: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface DiagnosticRuleSummary {
  ruleId: string;
  name: string;
  category: DiagnosticCategory;
  severity: FindingSeverity;
  enabled: boolean;
}

export interface DiagnosticFinding {
  findingId: string;
  reportId: string;
  ruleId?: string;
  title: string;
  description?: string;
  severity: FindingSeverity;
  metricName?: string;
  metricValue?: number;
  thresholdValue?: number;
  impact?: string;
  recommendation?: string;
  createdAt: string;
}

export interface DiagnosticReport {
  reportId: string;
  title: string;
  status: ReportStatus;
  startedAt?: string;
  completedAt?: string;
  totalFindings: number;
  infoCount: number;
  warningCount: number;
  criticalCount: number;
  fatalCount: number;
  summary?: string;
  createdBy?: string;
  createdAt: string;
}

export interface DiagnosticReportSummary {
  reportId: string;
  title: string;
  status: ReportStatus;
  totalFindings: number;
  warningCount: number;
  criticalCount: number;
  fatalCount: number;
  createdAt: string;
}

export interface DiagnosticReportWithFindings extends DiagnosticReport {
  findings: DiagnosticFinding[];
}

// ========== Rule APIs ==========

export const getDiagnosticRules = async (): Promise<
  DiagnosticRuleSummary[]
> => {
  const response =
    await request.get<DiagnosticRuleSummary[]>("/diagnostic/rules");
  return response.data;
};

export const getDiagnosticRulesByCategory = async (
  category: DiagnosticCategory,
): Promise<DiagnosticRuleSummary[]> => {
  const response = await request.get<DiagnosticRuleSummary[]>(
    `/diagnostic/rules/category/${category}`,
  );
  return response.data;
};

export const getDiagnosticRule = async (
  ruleId: string,
): Promise<DiagnosticRule> => {
  const response = await request.get<DiagnosticRule>(
    `/diagnostic/rules/${ruleId}`,
  );
  return response.data;
};

export interface CreateDiagnosticRuleParams {
  name: string;
  description?: string;
  category: DiagnosticCategory;
  condition: string;
  severity: FindingSeverity;
  recommendation?: string;
  enabled?: boolean;
}

export interface UpdateDiagnosticRuleParams {
  name?: string;
  description?: string;
  category?: DiagnosticCategory;
  condition?: string;
  severity?: FindingSeverity;
  recommendation?: string;
}

export const createDiagnosticRule = async (
  params: CreateDiagnosticRuleParams,
): Promise<DiagnosticRule> => {
  const response = await request.post<DiagnosticRule>(
    "/diagnostic/rules",
    params,
  );
  return response.data;
};

export const updateDiagnosticRule = async (
  ruleId: string,
  params: UpdateDiagnosticRuleParams,
): Promise<DiagnosticRule> => {
  const response = await request.put<DiagnosticRule>(
    `/diagnostic/rules/${ruleId}`,
    params,
  );
  return response.data;
};

export const enableDiagnosticRule = async (ruleId: string): Promise<void> => {
  await request.post(`/diagnostic/rules/${ruleId}/enable`);
};

export const disableDiagnosticRule = async (ruleId: string): Promise<void> => {
  await request.post(`/diagnostic/rules/${ruleId}/disable`);
};

export const deleteDiagnosticRule = async (ruleId: string): Promise<void> => {
  await request.delete(`/diagnostic/rules/${ruleId}`);
};

// ========== Report APIs ==========

export const getDiagnosticReports = async (): Promise<
  DiagnosticReportSummary[]
> => {
  const response = await request.get<DiagnosticReportSummary[]>(
    "/diagnostic/reports",
  );
  return response.data;
};

export const getRecentDiagnosticReports = async (
  limit: number = 10,
): Promise<DiagnosticReportSummary[]> => {
  const response = await request.get<DiagnosticReportSummary[]>(
    "/diagnostic/reports/recent",
    { params: { limit } },
  );
  return response.data;
};

export const getDiagnosticReport = async (
  reportId: string,
): Promise<DiagnosticReportWithFindings> => {
  const response = await request.get<DiagnosticReportWithFindings>(
    `/diagnostic/reports/${reportId}`,
  );
  return response.data;
};

export interface GenerateReportParams {
  title: string;
  createdBy?: string;
}

export const generateDiagnosticReport = async (
  params: GenerateReportParams,
): Promise<DiagnosticReport> => {
  const response = await request.post<DiagnosticReport>(
    "/diagnostic/reports",
    params,
  );
  return response.data;
};

export const generateDiagnosticReportAsync = async (
  params: GenerateReportParams,
): Promise<DiagnosticReport> => {
  const response = await request.post<DiagnosticReport>(
    "/diagnostic/reports/async",
    params,
  );
  return response.data;
};

export const deleteDiagnosticReport = async (
  reportId: string,
): Promise<void> => {
  await request.delete(`/diagnostic/reports/${reportId}`);
};
