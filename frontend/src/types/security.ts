// 安全基线检查相关类型

export interface BaselineTask {
  id: string;
  name: string;
  description?: string;
  templateId: string;
  templateName: string;
  targetType: "host" | "database" | "application" | "network";
  targetIds: string[];
  targetNames?: string[];
  environmentId?: string;
  environmentName?: string;
  schedule?: string;
  status: "pending" | "running" | "completed" | "failed" | "cancelled";
  lastRunTime?: string;
  nextRunTime?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
}

export interface BaselineTemplate {
  id: string;
  name: string;
  description?: string;
  level: "level2" | "level3" | "level4";
  category: "host" | "database" | "application" | "network";
  itemCount: number;
  isBuiltIn: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BaselineCheckItem {
  id: string;
  taskId: string;
  templateItemId: string;
  name: string;
  category: string;
  description: string;
  expectedValue: string;
  actualValue?: string;
  status: "compliant" | "non-compliant" | "not-applicable" | "error";
  severity: "critical" | "high" | "medium" | "low";
  remediation?: string;
  checkedAt?: string;
}

export interface BaselineResult {
  id: string;
  taskId: string;
  taskName: string;
  totalCount: number;
  compliantCount: number;
  nonCompliantCount: number;
  notApplicableCount: number;
  errorCount: number;
  complianceRate: number;
  status: "completed" | "partial" | "failed";
  startedAt: string;
  completedAt?: string;
  duration?: number;
  items: BaselineCheckItem[];
}

export interface BaselineReport {
  id: string;
  taskId: string;
  taskName: string;
  generatedAt: string;
  format: "pdf" | "word" | "html";
  downloadUrl?: string;
}

// 漏洞扫描相关类型

export interface VulnerabilityScanTask {
  id: string;
  name: string;
  description?: string;
  scanType: "host" | "web" | "database" | "container";
  targetIds: string[];
  targetNames?: string[];
  scannerType: "nmap" | "nessus" | "openvas" | "custom";
  status: "pending" | "running" | "completed" | "failed" | "cancelled";
  schedule?: string;
  lastRunTime?: string;
  nextRunTime?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
}

export interface Vulnerability {
  id: string;
  cveId?: string;
  name: string;
  description: string;
  severity: "critical" | "high" | "medium" | "low" | "info";
  cvssScore?: number;
  cvssVector?: string;
  affectedAssets: AffectedAsset[];
  solution?: string;
  references?: string[];
  disclosureDate?: string;
  discoveredAt: string;
  status: "new" | "confirmed" | "fixing" | "fixed" | "verified" | "ignored";
  assignedTo?: string;
  assignedToName?: string;
  fixedAt?: string;
  verifiedAt?: string;
  notes?: string;
}

export interface AffectedAsset {
  assetId: string;
  assetName: string;
  assetType: string;
  ipAddress?: string;
  port?: number;
  componentName?: string;
  componentVersion?: string;
}

export interface VulnerabilityTrend {
  date: string;
  total: number;
  critical: number;
  high: number;
  medium: number;
  low: number;
  fixed: number;
}

export interface VulnerabilityStats {
  total: number;
  critical: number;
  high: number;
  medium: number;
  low: number;
  fixRate: number;
  avgFixTime?: number;
}

// 合规自查相关类型

export interface ComplianceCheckItem {
  id: string;
  code: string;
  name: string;
  category: "technical" | "management";
  domain: string;
  requirement: string;
  checkMethod: string;
  evidence?: string;
  status: "compliant" | "non-compliant" | "partial" | "not-applicable";
  lastCheckedAt?: string;
  checker?: string;
  checkerName?: string;
  notes?: string;
}

export interface ComplianceSelfCheck {
  id: string;
  name: string;
  description?: string;
  level: "level2" | "level3" | "level4";
  status: "pending" | "in-progress" | "completed";
  totalCount: number;
  compliantCount: number;
  nonCompliantCount: number;
  partialCount: number;
  notApplicableCount: number;
  complianceRate: number;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
  items: ComplianceCheckItem[];
}

export interface ComplianceGapReport {
  id: string;
  checkId: string;
  checkName: string;
  generatedAt: string;
  gaps: ComplianceGap[];
  recommendations: string[];
  downloadUrl?: string;
}

export interface ComplianceGap {
  itemCode: string;
  itemName: string;
  requirement: string;
  currentStatus: string;
  gap: string;
  recommendation: string;
  priority: "critical" | "high" | "medium" | "low";
}

// 等保测评相关类型

export interface AssessmentDocument {
  id: string;
  name: string;
  type:
    | "system-description"
    | "topology-diagram"
    | "security-policy"
    | "audit-log"
    | "incident-record"
    | "training-record"
    | "other";
  format: "pdf" | "word" | "excel" | "pdf-template";
  generatedAt: string;
  generatedBy: string;
  downloadUrl?: string;
  expiresAt?: string;
}

export interface AssessmentQuestion {
  id: string;
  category: string;
  question: string;
  answer?: string;
  status: "pending" | "answered" | "confirmed";
  askedBy?: string;
  askedByName?: string;
  askedAt?: string;
  answeredBy?: string;
  answeredByName?: string;
  answeredAt?: string;
  attachments?: string[];
}

export interface RemediationTask {
  id: string;
  questionId?: string;
  gapItemId?: string;
  title: string;
  description: string;
  priority: "critical" | "high" | "medium" | "low";
  assignee?: string;
  assigneeName?: string;
  dueDate?: string;
  status: "pending" | "in-progress" | "completed" | "verified";
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
  completedAt?: string;
  verifiedAt?: string;
  notes?: string;
}

export interface AssessmentProgress {
  totalQuestions: number;
  answeredQuestions: number;
  confirmedQuestions: number;
  totalRemediationTasks: number;
  completedTasks: number;
  overdueTasks: number;
  estimatedCompletionDate?: string;
}

// API 请求/响应类型

export interface CreateBaselineTaskRequest {
  name: string;
  description?: string;
  templateId: string;
  targetType: "host" | "database" | "application" | "network";
  targetIds: string[];
  environmentId?: string;
  schedule?: string;
}

export interface UpdateBaselineTaskRequest {
  name?: string;
  description?: string;
  templateId?: string;
  targetIds?: string[];
  environmentId?: string;
  schedule?: string;
}

export interface ExecuteBaselineTaskRequest {
  taskId: string;
}

export interface CreateVulnerabilityScanRequest {
  name: string;
  description?: string;
  scanType: "host" | "web" | "database" | "container";
  targetIds: string[];
  scannerType: "nmap" | "nessus" | "openvas" | "custom";
  schedule?: string;
}

export interface UpdateVulnerabilityStatusRequest {
  vulnerabilityId: string;
  status: "confirmed" | "fixing" | "fixed" | "verified" | "ignored";
  assignedTo?: string;
  notes?: string;
}

export interface CreateSelfCheckRequest {
  name: string;
  description?: string;
  level: "level2" | "level3" | "level4";
}

export interface CheckItemRequest {
  checkId: string;
  itemId: string;
  status: "compliant" | "non-compliant" | "partial" | "not-applicable";
  evidence?: string;
  notes?: string;
}

export interface GenerateDocumentRequest {
  type: AssessmentDocument["type"];
  format: AssessmentDocument["format"];
  params?: Record<string, unknown>;
}

export interface CreateRemediationTaskRequest {
  questionId?: string;
  gapItemId?: string;
  title: string;
  description: string;
  priority: "critical" | "high" | "medium" | "low";
  assignee?: string;
  dueDate?: string;
}

export interface PaginatedResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}