import request from '../utils/request';
import type { SystemEventLogDTO, SystemEventLog, EventQueryCriteria, EventLogPage } from '../types/systemEventLog';

export const systemEventLogApi = {
  /**
   * 记录系统事件
   */
  logEvent(event: SystemEventLogDTO): Promise<void> {
    return request.post('/v1/system-event-logs', event);
  },

  /**
   * 查询系统事件日志
   */
  queryEvents(criteria: EventQueryCriteria): Promise<EventLogPage> {
    const params = new URLSearchParams();
    
    if (criteria.eventType) params.append('eventType', criteria.eventType);
    if (criteria.eventLevel) params.append('eventLevel', criteria.eventLevel);
    if (criteria.module) params.append('module', criteria.module);
    if (criteria.action) params.append('action', criteria.action);
    if (criteria.status) params.append('status', criteria.status);
    if (criteria.userId) params.append('userId', criteria.userId.toString());
    if (criteria.agentId) params.append('agentId', criteria.agentId.toString());
    if (criteria.goalId) params.append('goalId', criteria.goalId.toString());
    if (criteria.batchOperationId) params.append('batchOperationId', criteria.batchOperationId.toString());
    if (criteria.startTime) params.append('startTime', criteria.startTime);
    if (criteria.endTime) params.append('endTime', criteria.endTime);
    if (criteria.page) params.append('page', criteria.page.toString());
    if (criteria.size) params.append('size', criteria.size.toString());

    return request.get(`/v1/system-event-logs?${params.toString()}`);
  },

  /**
   * 获取事件详情
   */
  getEvent(id: number): Promise<SystemEventLog> {
    return request.get(`/v1/system-event-logs/${id}`);
  },

  /**
   * 清理旧日志
   */
  cleanupEvents(olderThanDays: number): Promise<void> {
    return request.delete(`/v1/system-event-logs?olderThanDays=${olderThanDays}`);
  },
};
