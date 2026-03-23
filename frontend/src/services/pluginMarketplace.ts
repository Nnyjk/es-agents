/**
 * 插件市场 API 服务
 */
import request from "@/utils/request";
import type {
  Plugin,
  InstalledPlugin,
  PluginConfig,
  PluginMetrics,
  PluginVersion,
  InstallProgress,
  PluginQueryParams,
  InstalledPluginQueryParams,
  PageResult,
} from "@/types/pluginMarketplace";

// =============== 插件市场 API ===============

const API_PREFIX = "/api/plugins";

/**
 * 获取插件市场列表
 */
export async function getPluginMarketList(
  params: PluginQueryParams,
): Promise<PageResult<Plugin>> {
  return request(`${API_PREFIX}/market`, {
    method: "GET",
    params,
  });
}

/**
 * 获取插件详情
 */
export async function getPluginDetail(pluginId: string): Promise<Plugin> {
  return request(`${API_PREFIX}/market/${pluginId}`, {
    method: "GET",
  });
}

/**
 * 获取插件版本列表
 */
export async function getPluginVersions(
  pluginId: string,
): Promise<PluginVersion[]> {
  return request(`${API_PREFIX}/market/${pluginId}/versions`, {
    method: "GET",
  });
}

/**
 * 安装插件
 */
export async function installPlugin(data: {
  pluginId: string;
  version?: string;
  agentId: string;
  config?: Record<string, unknown>;
}): Promise<void> {
  return request(`${API_PREFIX}/install`, {
    method: "POST",
    data,
  });
}

/**
 * 获取安装进度
 */
export async function getInstallProgress(
  pluginId: string,
): Promise<InstallProgress> {
  return request(`${API_PREFIX}/install/progress`, {
    method: "GET",
    params: { pluginId },
  });
}

/**
 * 取消安装
 */
export async function cancelInstall(pluginId: string): Promise<void> {
  return request(`${API_PREFIX}/install/cancel`, {
    method: "POST",
    params: { pluginId },
  });
}

/**
 * 检查插件更新
 */
export async function checkPluginUpdates(): Promise<Plugin[]> {
  return request(`${API_PREFIX}/updates`, {
    method: "GET",
  });
}

// =============== 已安装插件 API ===============

/**
 * 获取已安装插件列表
 */
export async function getInstalledPlugins(
  params: InstalledPluginQueryParams,
): Promise<PageResult<InstalledPlugin>> {
  return request(`${API_PREFIX}/installed`, {
    method: "GET",
    params,
  });
}

/**
 * 获取已安装插件详情
 */
export async function getInstalledPluginDetail(
  pluginId: string,
): Promise<InstalledPlugin> {
  return request(`${API_PREFIX}/installed/${pluginId}`, {
    method: "GET",
  });
}

/**
 * 卸载插件
 */
export async function uninstallPlugin(pluginId: string): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}`, {
    method: "DELETE",
  });
}

/**
 * 启用插件
 */
export async function enablePlugin(pluginId: string): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}/enable`, {
    method: "POST",
  });
}

/**
 * 禁用插件
 */
export async function disablePlugin(pluginId: string): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}/disable`, {
    method: "POST",
  });
}

/**
 * 更新插件
 */
export async function updatePlugin(
  pluginId: string,
  data: { version?: string; config?: Record<string, unknown> },
): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}/update`, {
    method: "POST",
    data,
  });
}

/**
 * 获取插件配置
 */
export async function getPluginConfig(pluginId: string): Promise<PluginConfig> {
  return request(`${API_PREFIX}/installed/${pluginId}/config`, {
    method: "GET",
  });
}

/**
 * 更新插件配置
 */
export async function updatePluginConfig(
  pluginId: string,
  config: Record<string, unknown>,
): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}/config`, {
    method: "PUT",
    data: config,
  });
}

/**
 * 获取插件日志
 */
export async function getPluginLogs(
  pluginId: string,
  params: { lines?: number; level?: string },
): Promise<{ logs: string }> {
  return request(`${API_PREFIX}/installed/${pluginId}/logs`, {
    method: "GET",
    params,
  });
}

/**
 * 获取插件运行指标
 */
export async function getPluginMetrics(
  pluginId: string,
): Promise<PluginMetrics> {
  return request(`${API_PREFIX}/installed/${pluginId}/metrics`, {
    method: "GET",
  });
}

/**
 * 重启插件
 */
export async function restartPlugin(pluginId: string): Promise<void> {
  return request(`${API_PREFIX}/installed/${pluginId}/restart`, {
    method: "POST",
  });
}

/**
 * 测试插件配置
 */
export async function testPluginConfig(
  pluginId: string,
  config: Record<string, unknown>,
): Promise<{ success: boolean; message: string }> {
  return request(`${API_PREFIX}/installed/${pluginId}/config/test`, {
    method: "POST",
    data: config,
  });
}

// =============== SSE 实时推送 ===============

/**
 * 订阅安装进度 SSE
 */
export function subscribeInstallProgress(
  pluginId: string,
  callbacks: {
    onProgress?: (progress: InstallProgress) => void;
    onComplete?: () => void;
    onError?: (error: Error) => void;
  },
): () => void {
  const eventSource = new EventSource(
    `${API_PREFIX}/install/progress/stream?pluginId=${pluginId}`,
  );

  eventSource.onmessage = (event) => {
    const progress: InstallProgress = JSON.parse(event.data);
    callbacks.onProgress?.(progress);

    if (progress.status === "completed") {
      callbacks.onComplete?.();
      eventSource.close();
    } else if (progress.status === "failed") {
      callbacks.onError?.(new Error(progress.error || "安装失败"));
      eventSource.close();
    }
  };

  eventSource.onerror = () => {
    callbacks.onError?.(new Error("SSE connection error"));
    eventSource.close();
  };

  return () => eventSource.close();
}

// =============== 搜索推荐 ===============

/**
 * 获取热门插件
 */
export async function getPopularPlugins(limit: number = 10): Promise<Plugin[]> {
  return request(`${API_PREFIX}/popular`, {
    method: "GET",
    params: { limit },
  });
}

/**
 * 获取推荐插件
 */
export async function getRecommendedPlugins(
  agentId?: string,
): Promise<Plugin[]> {
  return request(`${API_PREFIX}/recommended`, {
    method: "GET",
    params: { agentId },
  });
}

/**
 * 搜索插件
 */
export async function searchPlugins(
  keyword: string,
  limit: number = 10,
): Promise<Plugin[]> {
  return request(`${API_PREFIX}/search`, {
    method: "GET",
    params: { keyword, limit },
  });
}
