/**
 * Agent 资源来源相关类型定义
 * 用于 Agent 模板部署时获取软件包的来源配置
 */

import type { AgentCredentialSimple, AgentRepositorySimple } from "./index";

/**
 * Agent 资源来源类型枚举
 */
export type AgentSourceType =
  | "GITLAB"
  | "MAVEN"
  | "NEXTCLOUD"
  | "GIT"
  | "DOCKER"
  | "HTTPS"
  | "HTTP"
  | "LOCAL"
  | "ALIYUN";

/**
 * Agent 资源来源实体
 */
export interface AgentSource {
  id: string;
  name: string;
  type: AgentSourceType;
  config?: string;
  repository?: AgentRepositorySimple;
  repositoryId?: string;
  credential?: AgentCredentialSimple;
  credentialId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Agent 资源来源创建参数
 */
export interface AgentSourceCreate {
  name: string;
  type: AgentSourceType;
  config?: string;
  repositoryId?: string;
  credentialId?: string;
}

/**
 * Agent 资源来源更新参数
 */
export interface AgentSourceUpdate {
  name?: string;
  type?: AgentSourceType;
  config?: string;
  repositoryId?: string;
  credentialId?: string;
}

/**
 * Agent 资源来源查询参数
 */
export interface AgentSourceQueryParams {
  type?: AgentSourceType;
  keyword?: string;
}

/**
 * Agent 资源来源类型显示名称映射
 */
export const AgentSourceTypeLabels: Record<AgentSourceType, string> = {
  GITLAB: "GitLab 仓库",
  MAVEN: "Maven 仓库",
  NEXTCLOUD: "Nextcloud 仓库",
  GIT: "Git 仓库",
  DOCKER: "Docker 仓库",
  HTTPS: "HTTPS 资源",
  HTTP: "HTTP 资源",
  LOCAL: "本地文件",
  ALIYUN: "阿里云制品库",
};
