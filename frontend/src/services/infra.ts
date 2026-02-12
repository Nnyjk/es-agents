import request from '../utils/request';
import type { Host, Environment, PageParams, ListResponse } from '../types';

// Environments
export const queryEnvironments = async (params?: PageParams): Promise<Environment[] | ListResponse<Environment>> => {
  // 暂时直接返回列表，如果后端支持分页则返回 ListResponse
  return request.get('/infra/environments', { params });
};

export const saveEnvironment = (data: Partial<Environment>): Promise<Environment> => {
  if (data.id) {
    return request.put(`/infra/environments/${data.id}`, data);
  }
  return request.post('/infra/environments', data);
};

export const removeEnvironment = (id: string): Promise<void> => {
  return request.delete(`/infra/environments/${id}`);
};

// Hosts
export const queryHosts = async (params?: PageParams): Promise<Host[] | ListResponse<Host>> => {
  type HostApi = {
    id: string;
    name: string;
    hostname: string;
    os?: string;
    cpuInfo?: string;
    memInfo?: string;
    status: Host['status'];
    environmentId?: string;
    environmentName?: string;
    environment?: { id?: string; name?: string };
    secretKey?: string;
    description?: string;
    lastHeartbeat?: string;
    config?: string;
    heartbeatInterval?: number;
    gatewayUrl?: string;
    createdAt?: string;
    updatedAt?: string;
  };

  const res = (await request.get('/infra/hosts', { params })) as unknown as HostApi[] | ListResponse<HostApi>;
  const normalizeHost = (item: HostApi): Host => ({
    id: item.id,
    name: item.name,
    hostname: item.hostname,
    os: item.os,
    cpuInfo: item.cpuInfo,
    memInfo: item.memInfo,
    status: item.status,
    secretKey: item.secretKey,
    description: item.description,
    lastHeartbeat: item.lastHeartbeat,
    config: item.config,
    heartbeatInterval: item.heartbeatInterval,
    gatewayUrl: item.gatewayUrl,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    environmentId: item.environmentId ?? item.environment?.id,
    environmentName: item.environmentName ?? item.environment?.name,
    environment: {
      id: item.environmentId ?? item.environment?.id,
      name: item.environmentName ?? item.environment?.name,
    },
  });

  if (Array.isArray(res)) {
    return res.map(normalizeHost);
  }
  if (res && Array.isArray((res as ListResponse<HostApi>).data)) {
    return {
      ...(res as ListResponse<HostApi>),
      data: (res as ListResponse<HostApi>).data.map(normalizeHost),
    };
  }
  return [];
};

export const saveHost = (data: Partial<Host>): Promise<Host> => {
  const payload = {
    name: data.name,
    hostname: data.hostname,
    description: data.description,
    environmentId: data.environmentId ?? data.environment?.id,
    config: data.config,
    heartbeatInterval: data.heartbeatInterval,
    gatewayUrl: data.gatewayUrl,
  };
  if (data.id) {
    return request.put(`/infra/hosts/${data.id}`, payload);
  }
  return request.post('/infra/hosts', payload);
};

export const removeHost = (id: string): Promise<void> => {
  return request.delete(`/infra/hosts/${id}`);
};

export const connectHost = (id: string): Promise<void> => {
  return request.post(`/infra/hosts/${id}/connect`);
};

