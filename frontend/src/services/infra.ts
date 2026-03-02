import request from '../utils/request';
import axios from 'axios';
import type { Host, Environment, PageParams, ListResponse, HostInstallGuide } from '../types';

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
    os: data.os,
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

export const getInstallGuide = (id: string): Promise<HostInstallGuide> => {
  return request.get(`/infra/hosts/${id}/install-guide`);
};

/**
 * Download host agent package
 * @param downloadUrl API download URL (fallback)
 * @param githubReleaseUrl Direct GitHub Releases URL (preferred if available)
 * @param fileName File name for the download
 */
export const downloadHostPackage = async (
  downloadUrl: string | null | undefined,
  githubReleaseUrl: string | null | undefined,
  fileName: string
): Promise<void> => {
  // Prefer GitHub Releases URL if available (avoids 502 errors from server proxy)
  if (githubReleaseUrl && githubReleaseUrl.trim()) {
    const token = localStorage.getItem('token');
    const response = await axios.get(githubReleaseUrl, {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });

    const blobUrl = window.URL.createObjectURL(response.data);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
    return;
  }

  // Fallback to API endpoint
  if (!downloadUrl) {
    throw new Error('缺少下载地址');
  }
  
  const apiUrl = downloadUrl.startsWith('/api/') ? downloadUrl : `/api${downloadUrl}`;
  const token = localStorage.getItem('token');
  
  const response = await axios.get(apiUrl, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });

  const blobUrl = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);
};

// Host package download URL validation and normalization
const HOST_PACKAGE_DOWNLOAD_API_PATH = /^\/(?:api\/)?infra\/hosts\/[^/]+\/package(?:\?.*)?$/;

export const resolveHostPackageDownloadUrl = (downloadUrl: string): string => {
  if (!HOST_PACKAGE_DOWNLOAD_API_PATH.test(downloadUrl)) {
    throw new Error('安装引导返回了无效的部署包地址');
  }

  return downloadUrl.startsWith('/api/') ? downloadUrl : `/api${downloadUrl}`;
};
