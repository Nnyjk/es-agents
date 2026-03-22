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
  totalItems: number;
  compliantItems: number;
  nonCompliantItems: number;
  notApplicableItems: number;
  errorItems: number;
  complianceRate: number;
  startTime: string;
  endTime?: string;
  status: "running" | "completed" | "failed";
  items: BaselineCheckItem[];
  // Aliases for compatibility
  totalCount?: number;
  compliantCount?: number;
  nonCompliantCount?: number;
  duration?: number;
}

export interface BaselineReport {
  id: string;
  taskId: string;
  taskName: string;
  generatedAt: string;
  generatedBy: string;
  format: "pdf" | "word" | "html";
  downloadUrl: string;
  summary: {
    totalItems: number;
    compliantItems: number;
    nonCompliantItems: number;
    complianceRate: number;
  };
}

// 漏洞扫描相关类型

export interface VulnerabilityScanTask {
  id: string;
  name: string;
  description?: string;
  targetType: "host" | "network" | "web" | "database";
  targetIds: string[];
  targetNames?: string[];
  scanType: "quick" | "full" | "custom";
  scannerType: "nmap" | "nessus" | "openvas" | "awvs" | "custom";
  schedule?: string;
  status: "pending" | "running" | "completed" | "failed" | "cancelled";
  totalVulnerabilities?: number;
  criticalCount?: number;
  highCount?: number;
  mediumCount?: number;
  lowCount?: number;
  lastRunTime?: string;
  nextRunTime?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
}

export interface Vulnerability {
  id: string;
  taskId: string;
  cveId?: string;
  cnnvdId?: string;
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
  info: number;
  newCount: number;
  confirmedCount: number;
  fixingCount: number;
  fixedCount: number;
  verifiedCount: number;
  ignoredCount: number;
  fixRate: number;
  avgFixTime: number;
}

// 合规自查相关类型

export interface ComplianceStandard {
  id: string;
  name: string;
  code: string;
  version: string;
  description?: string;
  level: "level2" | "level3" | "level4";
  category: "host" | "database" | "application" | "network" | "management";
  itemCount: number;
  isBuiltIn: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ComplianceSelfCheck {
  id: string;
  name: string;
  standardId: string;
  standardName: string;
  description?: string;
  status: "pending" | "in-progress" | "completed" | "failed";
  totalItems: number;
  checkedItems: number;
  compliantItems: number;
  nonCompliantItems: number;
  notApplicableItems: number;
  complianceRate: number;
  startTime?: string;
  endTime?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByName?: string;
}

export interface ComplianceCheckItem {
  id: string;
  checkId: string;
  standardItemId: string;
  code: string;
  name: string;
  category: string;
  description: string;
  requirement: string;
  checkMethod: string;
  expectedValue: string;
  actualValue?: string;
  status: "pending" | "compliant" | "non-compliant" | "not-applicable";
  severity: "critical" | "high" | "medium" | "low";
  evidence?: string;
  remarks?: string;
  checkedAt?: string;
  checkedBy?: string;
  checkedByName?: string;
}

export interface ComplianceGapReport {
  id: string;
  checkId: string;
  checkName: string;
  generatedAt: string;
  generatedBy: string;
  format: "pdf" | "word" | "html";
  downloadUrl: string;
  summary: {
    totalItems: number;
    compliantItems: number;
    nonCompliantItems: number;
    notApplicableItems: number;
    complianceRate: number;
  };
  gaps: ComplianceGapItem[];
}

export interface ComplianceGapItem {
  id: string;
  code: string;
  name: string;
  category: string;
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
    | "registration"
    | "design"
    | "policy"
    | "self-assessment"
    | "gap-analysis"
    | "other";
  format?: "pdf" | "word" | "excel" | "pdf-template";
  status: "draft" | "pending" | "generating" | "completed" | "expired";
  category?: string;
  version?: string;
  evidence?: AssessmentEvidence[];
  generatedAt?: string;
  generatedBy?: string;
  generatedByName?: string;
  downloadUrl?: string;
  expiresAt?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AssessmentEvidence {
  id: string;
  name: string;
  type: string;
  url: string;
  uploadedAt: string;
  uploadedBy: string;
}

export interface AssessmentQuestion {
  id: string;
  category: string;
  question: string;
  answer?: string | null;
  evidence?: string | null;
  status: "pending" | "answered" | "confirmed";
  askedBy?: string;
  askedByName?: string;
  askedAt?: string;
  answeredBy?: string | null;
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
  status: "pending" | "in-progress" | "completed" | "verified";
  assignedTo?: string;
  assignee?: string;
  assignedToName?: string;
  dueDate?: string;
  completedAt?: string;
  verifiedAt?: string;
  createdAt: string;
  updatedAt?: string;
  createdBy: string;
  createdByName?: string;
  notes?: string;
  progress?: number;
  comments?: Array<{
    user: string;
    content: string;
    time: string;
  }>;
}

export interface AssessmentProgress {
  totalDocuments?: number;
  completedDocuments?: number;
  totalQuestions?: number;
  answeredQuestions?: number;
  totalTasks?: number;
  completedTasks?: number;
  overallProgress?: number;
  phase?: string;
  startDate?: string;
  expectedEndDate?: string;
  total?: number;
  completed?: number;
  pending?: number;
  inProgress?: number;
  percentage?: number;
  categories?: {
    name: string;
    total: number;
    completed: number;
    percentage: number;
  }[];
}

// 请求/响应类型

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
  schedule?: string;
}

export interface CreateVulnerabilityScanRequest {
  name: string;
  description?: string;
  targetType: "host" | "network" | "web" | "database";
  targetIds: string[];
  scannerType: "nmap" | "nessus" | "openvas" | "awvs" | "custom";
  schedule?: string;
}

export interface UpdateVulnerabilityStatusRequest {
  vulnerabilityId: string;
  status: "new" | "confirmed" | "fixing" | "fixed" | "verified" | "ignored";
  assignedTo?: string;
  notes?: string;
}

export interface CreateSelfCheckRequest {
  name: string;
  standardId: string;
  description?: string;
}

export interface CheckItemRequest {
  itemId: string;
  status: "compliant" | "non-compliant" | "not-applicable";
  actualValue?: string;
  evidence?: string;
  remarks?: string;
}

export interface GenerateDocumentRequest {
  type: AssessmentDocument["type"];
  format?: AssessmentDocument["format"];
  name?: string;
  description?: string;
}

export interface CreateRemediationTaskRequest {
  title: string;
  description: string;
  priority: "critical" | "high" | "medium" | "low";
  questionId?: string;
  gapItemId?: string;
  assignedTo?: string;
  dueDate?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}
