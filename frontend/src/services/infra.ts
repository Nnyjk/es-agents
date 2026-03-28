import request from "../utils/request";
import axios from "axios";
import type {
  Host,
  Environment,
  EnvironmentCreate,
  EnvironmentUpdate,
  PageParams,
  ListResponse,
  HostInstallGuide,
} from "../types";

// Environments - using /api/v1/environments
export const queryEnvironments = async (
  params?: PageParams,
): Promise<Environment[] | ListResponse<Environment>> => {
  return request.get("/api/v1/environments", { params });
};

export const getEnvironment = async (id: string): Promise<Environment> => {
  return request.get(`/api/v1/environments/${id}`);
};

export const createEnvironment = async (
  data: EnvironmentCreate,
): Promise<Environment> => {
  return request.post("/api/v1/environments", data);
};

export const updateEnvironment = async (
  id: string,
  data: EnvironmentUpdate,
): Promise<Environment> => {
  return request.put(`/api/v1/environments/${id}`, data);
};

export const removeEnvironment = (id: string): Promise<void> => {
  return request.delete(`/api/v1/environments/${id}`);
};

// Hosts
export const queryHosts = async (
  params?: PageParams,
): Promise<Host[] | ListResponse<Host>> => {
  type HostApi = {
    id: string;
    name: string;
    hostname: string;
    os?: string;
    status: Host["status"];
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

  const res = (await request.get("/v1/hosts", { params })) as unknown as
    | HostApi[]
    | ListResponse<HostApi>;
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
    return request.put(`/v1/hosts/${data.id}`, payload);
  }
  return request.post("/v1/hosts", payload);
};

export const removeHost = (id: string): Promise<void> => {
  return request.delete(`/v1/hosts/${id}`);
};

export const connectHost = (id: string): Promise<void> => {
  return request.post(`/v1/hosts/${id}/connect`);
};

export const getInstallGuide = (id: string): Promise<HostInstallGuide> => {
  return request.get(`/v1/hosts/${id}/install-guide`);
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
  fileName: string,
): Promise<void> => {
  // Prefer GitHub Releases URL if available (avoids 502 errors from server proxy)
  if (githubReleaseUrl && githubReleaseUrl.trim()) {
    const token = localStorage.getItem("token");
    const response = await axios.get(githubReleaseUrl, {
      responseType: "blob",
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    });

    const blobUrl = window.URL.createObjectURL(response.data);
    const link = document.createElement("a");
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
    throw new Error("缺少下载地址");
  }

  const apiUrl = downloadUrl.startsWith("/api/")
    ? downloadUrl
    : `/api${downloadUrl}`;
  const token = localStorage.getItem("token");

  const response = await axios.get(apiUrl, {
    responseType: "blob",
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });

  const blobUrl = window.URL.createObjectURL(response.data);
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);
};

// Host package download URL validation and normalization
const HOST_PACKAGE_DOWNLOAD_API_PATH =
  /^\/(?:api\/)?v1\/hosts\/[^/]+\/package(?:\?.*)?$/;

export const resolveHostPackageDownloadUrl = (downloadUrl: string): string => {
  if (!HOST_PACKAGE_DOWNLOAD_API_PATH.test(downloadUrl)) {
    throw new Error("安装引导返回了无效的部署包地址");
  }

  return downloadUrl.startsWith("/api/") ? downloadUrl : `/api${downloadUrl}`;
};
