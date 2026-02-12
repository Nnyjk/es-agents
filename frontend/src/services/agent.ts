import request from '../utils/request';
import type { AgentInstance, AgentTemplate, AgentCommand, AgentResource, ExecuteCommandParams, PageParams, ListResponse, AgentCredential, AgentRepository } from '../types';

// Agent Instances
export const queryAgentInstances = (params?: PageParams): Promise<AgentInstance[] | ListResponse<AgentInstance>> => {
  return request.get('/agents/instances', { params });
};

export const saveAgentInstance = (data: Partial<AgentInstance>): Promise<AgentInstance> => {
  if (data.id) {
    return request.put(`/agents/instances/${data.id}`, data);
  }
  return request.post('/agents/instances', data);
};

export const removeAgentInstance = (id: string): Promise<void> => {
  return request.delete(`/agents/instances/${id}`);
};

export const executeAgentCommand = (id: string, data: ExecuteCommandParams): Promise<void> => {
  return request.post(`/agents/instances/${id}/commands`, data);
};

// Agent Templates
export const queryAgentTemplates = (params?: { osType?: string; sourceType?: string }): Promise<AgentTemplate[]> => {
  return request.get('/agents/templates', { params });
};

export const saveAgentTemplate = (data: Partial<AgentTemplate>): Promise<AgentTemplate> => {
    if (data.id) {
        return request.put(`/agents/templates/${data.id}`, data);
    }
    return request.post('/agents/templates', data);
};

export const removeAgentTemplate = (id: string): Promise<void> => {
    return request.delete(`/agents/templates/${id}`);
};

// Agent Commands
export const queryAgentCommands = (templateId?: string): Promise<AgentCommand[]> => {
    return request.get('/agents/commands', { params: { templateId } });
};

export const saveAgentCommand = (data: Partial<AgentCommand>): Promise<AgentCommand> => {
    if (data.id) {
        const { templateId, ...payload } = data;
        return request.put(`/agents/commands/${data.id}`, payload);
    }
    return request.post('/agents/commands', data);
};

export const removeAgentCommand = (id: string): Promise<void> => {
    return request.delete(`/agents/commands/${id}`);
};

// Agent Resources (Sources)
export const queryAgentResources = (): Promise<AgentResource[]> => {
    return request.get('/agents/sources');
};

export const saveAgentResource = (data: Partial<AgentResource>): Promise<AgentResource> => {
    if (data.id) {
        return request.put(`/agents/sources/${data.id}`, data);
    }
    return request.post('/agents/sources', data);
};

export const removeAgentResource = (id: string): Promise<void> => {
    return request.delete(`/agents/sources/${id}`);
};

// Agent Credentials
export const queryAgentCredentials = (): Promise<AgentCredential[]> => {
  return request.get('/agents/credentials');
};

export const saveAgentCredential = (data: Partial<AgentCredential>): Promise<AgentCredential> => {
  if (data.id) {
    return request.put(`/agents/credentials/${data.id}`, data);
  }
  return request.post('/agents/credentials', data);
};

export const removeAgentCredential = (id: string): Promise<void> => {
  return request.delete(`/agents/credentials/${id}`);
};

// Agent Repositories
export const queryAgentRepositories = (): Promise<AgentRepository[]> => {
  return request.get('/agents/repositories');
};

export const saveAgentRepository = (data: Partial<AgentRepository>): Promise<AgentRepository> => {
  if (data.id) {
    return request.put(`/agents/repositories/${data.id}`, data);
  }
  return request.post('/agents/repositories', data);
};

export const removeAgentRepository = (id: string): Promise<void> => {
  return request.delete(`/agents/repositories/${id}`);
};
