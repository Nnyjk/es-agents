import request from "../utils/request";
import type {
  ScheduledTask,
  TaskExecution,
  ScheduledTaskQueryParams,
  CreateScheduledTaskParams,
  UpdateScheduledTaskParams,
} from "../types/scheduledTask";

const BASE_URL = "/scheduled-tasks";

// 获取定时任务列表
export async function getScheduledTasks(
  params?: ScheduledTaskQueryParams
): Promise<{ data: ScheduledTask[]; total: number }> {
  const response = await request.get(BASE_URL, { params });
  return response.data;
}

// 获取单个定时任务详情
export async function getScheduledTask(id: string): Promise<ScheduledTask> {
  const response = await request.get(`${BASE_URL}/${id}`);
  return response.data;
}

// 创建定时任务
export async function createScheduledTask(
  data: CreateScheduledTaskParams
): Promise<ScheduledTask> {
  const response = await request.post(BASE_URL, data);
  return response.data;
}

// 更新定时任务
export async function updateScheduledTask(
  id: string,
  data: UpdateScheduledTaskParams
): Promise<ScheduledTask> {
  const response = await request.put(`${BASE_URL}/${id}`, data);
  return response.data;
}

// 删除定时任务
export async function deleteScheduledTask(id: string): Promise<void> {
  await request.delete(`${BASE_URL}/${id}`);
}

// 启用定时任务
export async function enableScheduledTask(id: string): Promise<ScheduledTask> {
  const response = await request.post(`${BASE_URL}/${id}/enable`);
  return response.data;
}

// 禁用定时任务
export async function disableScheduledTask(
  id: string
): Promise<ScheduledTask> {
  const response = await request.post(`${BASE_URL}/${id}/disable`);
  return response.data;
}

// 手动触发执行定时任务
export async function triggerScheduledTask(
  id: string
): Promise<{ executionId: string }> {
  const response = await request.post(`${BASE_URL}/${id}/trigger`);
  return response.data;
}

// 获取任务执行记录列表
export async function getTaskExecutions(
  taskId: string,
  params?: { limit?: number; offset?: number }
): Promise<{ data: TaskExecution[]; total: number }> {
  const response = await request.get(`${BASE_URL}/${taskId}/executions`, {
    params,
  });
  return response.data;
}

// 获取单个执行记录详情
export async function getTaskExecution(
  taskId: string,
  executionId: string
): Promise<TaskExecution> {
  const response = await request.get(
    `${BASE_URL}/${taskId}/executions/${executionId}`
  );
  return response.data;
}

// 获取任务执行统计
export async function getTaskExecutionStats(taskId: string): Promise<{
  totalExecutions: number;
  successCount: number;
  failedCount: number;
  avgDuration: number;
  successRate: number;
}> {
  const response = await request.get(`${BASE_URL}/${taskId}/stats`);
  return response.data;
}