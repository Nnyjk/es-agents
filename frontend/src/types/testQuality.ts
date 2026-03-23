/**
 * 测试与质量管控管理类型定义
 */

// ============ 测试用例管理相关类型 ============

/** 测试用例优先级 */
export type TestCasePriority = 'P0' | 'P1' | 'P2' | 'P3';

/** 测试用例类型 */
export type TestCaseType = 'functional' | 'performance' | 'security' | 'compatibility' | 'integration' | 'unit';

/** 测试用例状态 */
export type TestCaseStatus = 'draft' | 'reviewing' | 'approved' | 'deprecated';

/** 测试用例 */
export interface TestCase {
  id: string;
  name: string;
  description: string;
  type: TestCaseType;
  priority: TestCasePriority;
  status: TestCaseStatus;
  module: string;
  tags: string[];
  preconditions: string;
  steps: TestStep[];
  expectedResults: string;
  createdAt: string;
  updatedAt: string;
  creator: string;
  version: number;
}

/** 测试步骤 */
export interface TestStep {
  step: number;
  action: string;
  expectedResult: string;
}

/** 测试套件状态 */
export type TestSuiteStatus = 'active' | 'inactive' | 'archived';

/** 测试套件 */
export interface TestSuite {
  id: string;
  name: string;
  description: string;
  testCases: string[];
  environment: string;
  status: TestSuiteStatus;
  createdAt: string;
  updatedAt: string;
  creator: string;
}

/** 测试计划 */
export interface TestPlan {
  id: string;
  name: string;
  description: string;
  testSuites: string[];
  startDate: string;
  endDate: string;
  status: 'planning' | 'executing' | 'completed' | 'cancelled';
  createdAt: string;
  updatedAt: string;
  creator: string;
}

/** 用例执行记录 */
export interface TestCaseExecution {
  id: string;
  testCaseId: string;
  testCaseName: string;
  testPlanId: string;
  status: 'passed' | 'failed' | 'blocked' | 'skipped';
  duration: number;
  executedAt: string;
  executor: string;
  errorMessage?: string;
  screenshot?: string;
}

// ============ 自动化测试任务管理相关类型 ============

/** 任务执行策略 */
export type ExecutionStrategy = 'sequential' | 'parallel';

/** 任务触发条件 */
export type TriggerCondition = 'manual' | 'scheduled' | 'webhook' | 'commit';

/** 自动化测试任务 */
export interface TestTask {
  id: string;
  name: string;
  description: string;
  testSuiteId: string;
  testSuiteName: string;
  environment: string;
  strategy: ExecutionStrategy;
  triggerCondition: TriggerCondition;
  schedule?: string;
  status: 'idle' | 'running' | 'completed' | 'failed' | 'cancelled';
  lastExecutionAt?: string;
  lastExecutionStatus?: 'passed' | 'failed' | 'partial';
  passRate?: number;
  createdAt: string;
  updatedAt: string;
  creator: string;
}

/** 任务执行详情 */
export interface TaskExecution {
  id: string;
  taskId: string;
  taskName: string;
  status: 'running' | 'completed' | 'failed' | 'cancelled' | 'paused';
  startTime: string;
  endTime?: string;
  duration?: number;
  totalCases: number;
  passedCases: number;
  failedCases: number;
  skippedCases: number;
  currentStep?: string;
  progress: number;
  logs: ExecutionLog[];
  executor?: string;
  triggerType?: string;
  failedCaseDetails?: FailedCaseDetail[];
}

/** 失败用例详情 */
export interface FailedCaseDetail {
  id: string;
  caseName: string;
  failureReason: string;
}

/** 执行日志 */
export interface ExecutionLog {
  timestamp: string;
  level: 'info' | 'warning' | 'error';
  message: string;
}

// ============ 测试结果与缺陷管理相关类型 ============

/** 测试报告 */
export interface TestReport {
  id: string;
  name: string;
  testPlanId: string;
  testPlanName: string;
  executionId: string;
  status: 'passed' | 'failed' | 'partial';
  totalCases: number;
  passedCases: number;
  failedCases: number;
  skippedCases: number;
  blockedCases: number;
  passRate: number;
  duration: number;
  coverage: number;
  executedAt: string;
  executor: string;
  environment: string;
  summary: string;
  defects: string[];
  // 扩展属性
  startDate?: string;
  taskName?: string;
  executionTime?: string;
  triggerType?: string;
  caseDetails?: ReportCase[];
}

/** 缺陷严重程度 */
export type DefectSeverity = 'critical' | 'major' | 'normal' | 'minor';

/** 缺陷状态 */
export type DefectStatus = 'open' | 'in_progress' | 'resolved' | 'verified' | 'closed' | 'rejected';

/** 缺陷 */
export interface Defect {
  id: string;
  title: string;
  description: string;
  severity: DefectSeverity;
  status: DefectStatus;
  testCaseId?: string;
  testCaseName?: string;
  testReportId?: string;
  assignee?: string;
  reporter: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  environment?: string;
  steps?: string[];
  attachments?: string[];
}

// ============ 质量度量与分析相关类型 ============

/** 质量指标 */
export interface QualityMetrics {
  requirementCoverage: number;
  casePassRate: number;
  defectEscapeRate: number;
  avgFixTime: number;
  deliveryCycle: number;
  defectDensity: number;
  testCoverage: number;
  automationRate: number;
}

/** 质量趋势数据 */
export interface QualityTrend {
  date: string;
  passRate: number;
  defectCount: number;
  coverage: number;
  avgFixTime: number;
}

/** 质量门禁 */
export interface QualityGate {
  id: string;
  name: string;
  stage: 'dev' | 'test' | 'staging' | 'production';
  conditions: GateCondition[];
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/** 门禁条件 */
export interface GateCondition {
  metric: string;
  operator: '>' | '>=' | '<' | '<=' | '==' | '!=';
  threshold: number;
}

/** 质量报表 */
export interface QualityReport {
  id: string;
  name: string;
  type: 'daily' | 'weekly' | 'monthly' | 'release';
  period: string;
  metrics: QualityMetrics;
  trends: QualityTrend[];
  highlights: string[];
  issues: string[];
  createdAt: string;
  creator: string;
}

// ============ CI/CD流水线集成相关类型 ============

/** 流水线状态 */
export type PipelineStatus = 'pending' | 'running' | 'success' | 'failed' | 'cancelled';

/** 流水线阶段 */
export interface PipelineStage {
  name: string;
  status: PipelineStatus;
  startTime?: string;
  endTime?: string;
  duration?: number;
  qualityGatePassed?: boolean;
  gateDetails?: GateCheckResult[];
}

/** 门禁检查结果 */
export interface GateCheckResult {
  condition: string;
  expected: string;
  actual: string;
  passed: boolean;
}

/** CI/CD流水线 */
export interface Pipeline {
  id: string;
  name: string;
  project: string;
  environment: string;
  status: PipelineStatus;
  stages: PipelineStage[];
  currentStage?: string;
  triggerBy: string;
  triggerType: 'manual' | 'webhook' | 'scheduled';
  branch: string;
  commit: string;
  commitMessage: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  testResults?: PipelineTestResults;
  qualityGatesPassed: boolean;
}

/** 流水线测试结果 */
export interface PipelineTestResults {
  totalCases: number;
  passedCases: number;
  failedCases: number;
  skippedCases: number;
  passRate: number;
  coverage: number;
}

/** 流水线配置 */
export interface PipelineConfig {
  id: string;
  name: string;
  project: string;
  testStage: string;
  testSuiteId: string;
  qualityGateId: string;
  triggerOnCommit: boolean;
  triggerBranches: string[];
  createdAt: string;
  updatedAt: string;
}

/** 发布版本 */
export interface ReleaseVersion {
  id: string;
  version: string;
  project: string;
  releaseDate: string;
  testResults: PipelineTestResults;
  defectCount: number;
  resolvedDefectCount: number;
  qualityScore: number;
  pipelines: string[];
}

// ============ 通用类型 ============

/** 分页参数 */
export interface PageParams {
  page: number;
  pageSize: number;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  status?: string;
}

/** 分页结果 */
export interface PageResult<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
}

/** API 响应 */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
}

/** 报告趋势数据 */
export interface ReportTrend {
  date: string;
  totalCases: number;
  passedCases: number;
  failedCases: number;
  passRate: number;
  executionCount: number;
}

/** 环境配置 */
export interface TestEnvironment {
  id: string;
  name: string;
  type: 'http' | 'database' | 'redis' | 'mq' | 'sftp';
  host: string;
  port: number;
  username?: string;
  password?: string;
  status: 'active' | 'inactive' | 'error';
  description?: string;
  createdAt: string;
  updatedAt: string;
}

/** 全局配置 */
export interface GlobalConfig {
  defaultTimeout: number;
  retryCount: number;
  maxConcurrent: number;
  reportRetentionDays: number;
  baseAssertion?: string;
  globalVariables?: string;
}

/** 通知配置 */
export interface NotificationConfig {
  enableEmail: boolean;
  emailRecipients: string[];
  enableWebhook: boolean;
  webhookUrl?: string;
  webhookTemplate?: string;
  notifyOnSuccess: boolean;
  notifyOnFailure: boolean;
  notifyOnTimeout: boolean;
}

/** 执行统计 */
export interface ExecutionStatistics {
  todayExecutions: number;
  runningCount: number;
  successRate: number;
  avgDuration: number;
}

/** 报告用例详情 */
export interface ReportCase {
  id: string;
  caseId: string;
  caseName: string;
  moduleName: string;
  status: 'passed' | 'failed' | 'blocked' | 'skipped';
  duration: number;
  errorMessage?: string;
  executedAt: string;
  executor: string;
}