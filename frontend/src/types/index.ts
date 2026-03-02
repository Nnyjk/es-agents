export interface CurrentUser {
  id: string;
  username: string;
  roles: string[];
  permissions?: {
    menus: string[];
    actions: string[];
  };
}

export interface LoginResult {
  token: string;
  userInfo: CurrentUser;
  permissions: {
    menus: string[];
    actions: string[];
  };
}

export interface User {
  id: string;
  username: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  roles: Role[];
  roleIds?: string[];
  password?: string;
}

export interface Role {
  id: string;
  code: string;
  name: string;
  description?: string;
  moduleIds?: string[];
  actionIds?: string[];
}

export interface Module {
  id: string;
  parentId?: string;
  name: string;
  code: string;
  type: 'DIRECTORY' | 'MENU' | 'BUTTON';
  path?: string;
  icon?: string;
  sortOrder?: number;
  children?: Module[];
}

import { Host } from './infrastructure';

export interface AgentCredential {
  id: string;
  name: string;
  type: 'STATIC_TOKEN' | 'API_TOKEN' | 'SCRIPT_TOKEN' | 'SSO_TOKEN';
  config: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentCredentialSimple {
  id: string;
  name: string;
  type: 'STATIC_TOKEN' | 'API_TOKEN' | 'SCRIPT_TOKEN' | 'SSO_TOKEN';
}

export interface AgentRepository {
  id: string;
  name: string;
  type: 'GITLAB' | 'MAVEN' | 'NEXTCLOUD';
  baseUrl: string;
  projectPath: string;
  defaultBranch?: string;
  credential?: AgentCredentialSimple;
  credentialId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentRepositorySimple {
  id: string;
  name: string;
  type: 'GITLAB' | 'MAVEN' | 'NEXTCLOUD';
}

export interface AgentResource {
  id: string;
  name: string;
  type: 'GITLAB' | 'MAVEN' | 'NEXTCLOUD' | 'GIT' | 'DOCKER' | 'HTTPS' | 'HTTP' | 'LOCAL' | 'ALIYUN';
  config: string;
  repository?: AgentRepositorySimple;
  repositoryId?: string;
  credential?: AgentCredentialSimple;
  credentialId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentCommand {
  id: string;
  name: string;
  script: string;
  timeout: number;
  defaultArgs?: string;
  templateId?: string;
  hostId?: string;
}

export interface AgentTemplate {
  id: string;
  name: string;
  description?: string;
  osType?: string;
  source?: AgentResource;
  sourceId?: string;
  commands?: AgentCommand[];
  createdAt: string;
  updatedAt: string;
}

export interface AgentInstance {
  id: string;
  host: Host;
  hostId?: string; // Form usage
  template: AgentTemplate;
  templateId?: string; // Form usage
  status: 'OFFLINE' | 'ONLINE' | 'BUSY' | 'UNCONFIGURED';
  version?: string;
  lastHeartbeatTime?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ExecuteCommandParams {
  commandId: string;
  args?: string;
}

export interface PageParams {
  current?: number;
  pageSize?: number;
}

export interface ListResponse<T> {
  data: T[];
  total: number;
  success: boolean;
}

// 基础设施相关类型
export * from './infrastructure';
