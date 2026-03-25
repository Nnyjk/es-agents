import request from "../utils/request";
import axios from "axios";
import type {
  AgentInstance,
  AgentTemplate,
  AgentTemplateCreate,
  AgentTemplateUpdate,
  AgentTemplateQueryParams,
  AgentCommand,
  AgentResource,
  ExecuteCommandParams,
  PageParams,
  ListResponse,
  AgentCredential,
  AgentRepository,
  DeployParams,
  DeployResult,
  AgentTask,
  AgentTaskQueryParams,
} from "../types";

const AGENT_TEMPLATE_DOWNLOAD_API_PATH =
  /^\/(?:api\/)?agents\/templates\/[^/]+\/download(?:\?.*)?$/;

const triggerBrowserDownload = (blob: Blob, fileName: string): void => {
  const blobUrl = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);
};

const extractFileName = (contentDisposition?: string | null): string | null => {
  if (!contentDisposition) {
    return null;
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const asciiMatch =
    contentDisposition.match(/filename="([^"]+)"/i) ??
    contentDisposition.match(/filename=([^;]+)/i);
  return asciiMatch?.[1]?.trim() ?? null;
};

// Agent Instances
export const queryAgentInstances = (
  params?: PageParams & { hostId?: string },
): Promise<AgentInstance[] | ListResponse<AgentInstance>> => {
  return request.get("/agents/instances", { params });
};

export const saveAgentInstance = (
  data: Partial<AgentInstance>,
): Promise<AgentInstance> => {
  if (data.id) {
    return request.put(`/agents/instances/${data.id}`, data);
  }
  return request.post("/agents/instances", data);
};

export const removeAgentInstance = (id: string): Promise<void> => {
  return request.delete(`/agents/instances/${id}`);
};

// Agent 部署
export const deployAgentInstance = (
  id: string,
  data: DeployParams,
): Promise<DeployResult> => {
  return request.post(`/agents/instances/${id}/deploy`, data);
};

export const getAgentTaskHistory = async (
  instanceId: string,
): Promise<AgentTask[]> => {
  const response = await request.get(`/agents/instances/${instanceId}/tasks`);
  return response.data || response;
};

export const executeAgentCommand = (
  id: string,
  data: ExecuteCommandParams,
): Promise<void> => {
  return request.post(`/agents/instances/${id}/commands`, data);
};

// Agent Templates
export const queryAgentTemplates = (params?: AgentTemplateQueryParams): Promise<AgentTemplate[]> => {
  return request.get("/agents/templates", { params });
};

export const getAgentTemplate = (id: string): Promise<AgentTemplate> => {
  return request.get(`/agents/templates/${id}`);
};

export const createAgentTemplate = (data: AgentTemplateCreate): Promise<AgentTemplate> => {
  return request.post("/agents/templates", data);
};

export const updateAgentTemplate = (id: string, data: AgentTemplateUpdate): Promise<AgentTemplate> => {
  return request.put(`/agents/templates/${id}`, data);
};

export const saveAgentTemplate = (
  data: Partial<AgentTemplate> | AgentTemplateCreate | AgentTemplateUpdate,
): Promise<AgentTemplate> => {
  if ('id' in data && data.id) {
    const { id, ...payload } = data;
    return request.put(`/agents/templates/${id}`, payload);
  }
  return request.post("/agents/templates", data);
};

export const removeAgentTemplate = (id: string): Promise<void> => {
  return request.delete(`/agents/templates/${id}`);
};

export const importAgentTemplate = async (file: File): Promise<AgentTemplate> => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/agents/templates/import", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const exportAgentTemplate = async (id: string): Promise<void> => {
  const token = localStorage.getItem("token");
  const response = await axios.get(
    `/api/agents/templates/${id}/export`,
    {
      responseType: "blob",
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    },
  );
  
  const responseFileName = extractFileName(
    response.headers["content-disposition"],
  );
  triggerBrowserDownload(response.data, responseFileName || `template-${id}.json`);
};

export const resolveAgentTemplateDownloadUrl = (
  downloadUrl: string,
): string => {
  if (!AGENT_TEMPLATE_DOWNLOAD_API_PATH.test(downloadUrl)) {
    throw new Error("模板下载地址无效");
  }

  return downloadUrl.startsWith("/api/") ? downloadUrl : `/api${downloadUrl}`;
};

export const downloadAgentTemplate = async (
  id: string,
  fallbackFileName = "agent-package",
): Promise<void> => {
  const token = localStorage.getItem("token");
  const response = await axios.get(
    resolveAgentTemplateDownloadUrl(`/agents/templates/${id}/download`),
    {
      responseType: "blob",
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    },
  );

  const responseFileName = extractFileName(
    response.headers["content-disposition"],
  );
  triggerBrowserDownload(response.data, responseFileName || fallbackFileName);
};

// Agent Commands
export const queryAgentCommands = (
  templateId?: string,
): Promise<AgentCommand[]> => {
  return request.get("/agents/commands", { params: { templateId } });
};

export const saveAgentCommand = (
  data: Partial<AgentCommand>,
): Promise<AgentCommand> => {
  if (data.id) {
    const { templateId, ...payload } = data;
    return request.put(`/agents/commands/${data.id}`, payload);
  }
  return request.post("/agents/commands", data);
};

export const removeAgentCommand = (id: string): Promise<void> => {
  return request.delete(`/agents/commands/${id}`);
};

// Agent Resources (Sources)
export const queryAgentResources = (): Promise<AgentResource[]> => {
  return request.get("/agents/sources");
};

export const saveAgentResource = (
  data: Partial<AgentResource>,
): Promise<AgentResource> => {
  if (data.id) {
    return request.put(`/agents/sources/${data.id}`, data);
  }
  return request.post("/agents/sources", data);
};

export const removeAgentResource = (id: string): Promise<void> => {
  return request.delete(`/agents/sources/${id}`);
};

// Agent Credentials
export const queryAgentCredentials = (): Promise<AgentCredential[]> => {
  return request.get("/agents/credentials");
};

export const saveAgentCredential = (
  data: Partial<AgentCredential>,
): Promise<AgentCredential> => {
  if (data.id) {
    return request.put(`/agents/credentials/${data.id}`, data);
  }
  return request.post("/agents/credentials", data);
};

export const removeAgentCredential = (id: string): Promise<void> => {
  return request.delete(`/agents/credentials/${id}`);
};

// Agent Repositories
export const queryAgentRepositories = (): Promise<AgentRepository[]> => {
  return request.get("/agents/repositories");
};

export const saveAgentRepository = (
  data: Partial<AgentRepository>,
): Promise<AgentRepository> => {
  if (data.id) {
    return request.put(`/agents/repositories/${data.id}`, data);
  }
  return request.post("/agents/repositories", data);
};

export const removeAgentRepository = (id: string): Promise<void> => {
  return request.delete(`/agents/repositories/${id}`);
};

// Agent Tasks - 命令执行与任务查询
export const executeCommand = async (
  instanceId: string,
  params: ExecuteCommandParams,
): Promise<AgentTask> => {
  const response = await request.post<AgentTask>(
    `/agents/instances/${instanceId}/execute`,
    params,
  );
  return response.data;
};

export const getAgentTask = async (taskId: string): Promise<AgentTask> => {
  const response = await request.get<AgentTask>(`/agents/tasks/${taskId}`);
  return response.data;
};

export const queryAgentTasks = async (
  params: AgentTaskQueryParams,
): Promise<ListResponse<AgentTask>> => {
  const response = await request.get<ListResponse<AgentTask>>("/agents/tasks", {
    params,
  });
  return response.data;
};

export const cancelAgentTask = async (taskId: string): Promise<AgentTask> => {
  const response = await request.post<AgentTask>(
    `/agents/tasks/${taskId}/cancel`,
  );
  return response.data;
};

export const getAgentTaskLogs = async (taskId: string): Promise<string> => {
  const response = await request.get<string>(`/agents/tasks/${taskId}/logs`);
  return response.data;
};
