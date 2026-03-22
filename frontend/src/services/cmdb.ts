/**
 * CMDB 资源与资产管理 API 服务
 */
import request from "../utils/request";
import type {
  ResourceTypeConfig,
  Resource,
  ResourceRelation,
  ResourceTopology,
  ResourceQueryParams,
  ResourceImportResult,
  ResourceChangeHistory,
  ExpiryReminder,
  Environment,
  Department,
  PageResult,
  CustomFieldDefinition,
} from "@/types/cmdb";

// ============== 资源类型管理 API ==============

/**
 * 获取资源类型列表
 */
export async function getResourceTypes(): Promise<ResourceTypeConfig[]> {
  return request.get("/api/cmdb/resource-types");
}

/**
 * 获取资源类型详情
 */
export async function getResourceType(id: string): Promise<ResourceTypeConfig> {
  return request.get(`/api/cmdb/resource-types/${id}`);
}

/**
 * 创建资源类型
 */
export async function createResourceType(
  data: Omit<ResourceTypeConfig, "id" | "createdAt" | "updatedAt">,
): Promise<ResourceTypeConfig> {
  return request.post("/api/cmdb/resource-types", data);
}

/**
 * 更新资源类型
 */
export async function updateResourceType(
  id: string,
  data: Partial<ResourceTypeConfig>,
): Promise<ResourceTypeConfig> {
  return request.put(`/api/cmdb/resource-types/${id}`, data);
}

/**
 * 删除资源类型
 */
export async function deleteResourceType(id: string): Promise<void> {
  return request.delete(`/api/cmdb/resource-types/${id}`);
}

/**
 * 添加自定义字段到资源类型
 */
export async function addCustomField(
  typeId: string,
  field: Omit<CustomFieldDefinition, "id">,
): Promise<CustomFieldDefinition> {
  return request.post(`/api/cmdb/resource-types/${typeId}/fields`, field);
}

/**
 * 更新自定义字段
 */
export async function updateCustomField(
  typeId: string,
  fieldId: string,
  data: Partial<CustomFieldDefinition>,
): Promise<CustomFieldDefinition> {
  return request.put(
    `/api/cmdb/resource-types/${typeId}/fields/${fieldId}`,
    data,
  );
}

/**
 * 删除自定义字段
 */
export async function deleteCustomField(
  typeId: string,
  fieldId: string,
): Promise<void> {
  return request.delete(`/api/cmdb/resource-types/${typeId}/fields/${fieldId}`);
}

// ============== 资源台账管理 API ==============

/**
 * 查询资源列表
 */
export async function queryResources(
  params: ResourceQueryParams,
): Promise<PageResult<Resource>> {
  return request.get("/api/cmdb/resources", { params });
}

/**
 * 获取资源详情
 */
export async function getResource(id: string): Promise<Resource> {
  return request.get(`/api/cmdb/resources/${id}`);
}

/**
 * 创建资源
 */
export async function createResource(
  data: Omit<
    Resource,
    "id" | "createdAt" | "updatedAt" | "createdBy" | "updatedBy"
  >,
): Promise<Resource> {
  return request.post("/api/cmdb/resources", data);
}

/**
 * 更新资源
 */
export async function updateResource(
  id: string,
  data: Partial<Resource>,
): Promise<Resource> {
  return request.put(`/api/cmdb/resources/${id}`, data);
}

/**
 * 删除资源
 */
export async function deleteResource(id: string): Promise<void> {
  return request.delete(`/api/cmdb/resources/${id}`);
}

/**
 * 批量删除资源
 */
export async function batchDeleteResources(ids: string[]): Promise<void> {
  return request.post("/api/cmdb/resources/batch-delete", { ids });
}

/**
 * 批量更新资源状态
 */
export async function batchUpdateStatus(
  ids: string[],
  status: string,
): Promise<void> {
  return request.post("/api/cmdb/resources/batch-status", { ids, status });
}

/**
 * 导入资源
 */
export async function importResources(
  file: File,
): Promise<ResourceImportResult> {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/api/cmdb/resources/import", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 导出资源
 */
export async function exportResources(
  params: Omit<ResourceQueryParams, "current" | "pageSize">,
): Promise<Blob> {
  return request.get("/api/cmdb/resources/export", {
    params,
    responseType: "blob",
  });
}

/**
 * 下载导入模板
 */
export async function downloadImportTemplate(): Promise<Blob> {
  return request.get("/api/cmdb/resources/template", {
    responseType: "blob",
  });
}

// ============== 资源关联关系 API ==============

/**
 * 获取资源的关联关系
 */
export async function getResourceRelations(
  resourceId: string,
): Promise<ResourceRelation[]> {
  return request.get(`/api/cmdb/resources/${resourceId}/relations`);
}

/**
 * 创建资源关联
 */
export async function createResourceRelation(
  data: Omit<
    ResourceRelation,
    "id" | "createdAt" | "createdBy" | "sourceName" | "targetName"
  >,
): Promise<ResourceRelation> {
  return request.post("/api/cmdb/relations", data);
}

/**
 * 删除资源关联
 */
export async function deleteResourceRelation(id: string): Promise<void> {
  return request.delete(`/api/cmdb/relations/${id}`);
}

/**
 * 获取资源拓扑图
 */
export async function getResourceTopology(
  resourceId?: string,
): Promise<ResourceTopology> {
  const params = resourceId ? { resourceId } : {};
  return request.get("/api/cmdb/topology", { params });
}

// ============== 资源生命周期 API ==============

/**
 * 获取资源变更历史
 */
export async function getResourceChangeHistory(
  resourceId: string,
): Promise<ResourceChangeHistory[]> {
  return request.get(`/api/cmdb/resources/${resourceId}/history`);
}

/**
 * 获取到期提醒列表
 */
export async function getExpiryReminders(): Promise<ExpiryReminder[]> {
  return request.get("/api/cmdb/reminders");
}

/**
 * 更新到期提醒状态
 */
export async function updateReminderNotified(
  id: string,
  notified: boolean,
): Promise<void> {
  return request.put(`/api/cmdb/reminders/${id}`, { notified });
}

// ============== 基础数据 API ==============

/**
 * 获取环境列表
 */
export async function getEnvironments(): Promise<Environment[]> {
  return request.get("/api/cmdb/environments");
}

/**
 * 获取部门列表
 */
export async function getDepartments(): Promise<Department[]> {
  return request.get("/api/cmdb/departments");
}

/**
 * 获取资源统计
 */
export async function getResourceStatistics(): Promise<{
  total: number;
  byType: Record<string, number>;
  byStatus: Record<string, number>;
  byEnvironment: Record<string, number>;
}> {
  return request.get("/api/cmdb/statistics");
}
