package com.easystation.agent.tool.repository;

import com.easystation.agent.tool.domain.ToolExecutionLog;
import com.easystation.agent.tool.domain.ToolExecutionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具执行日志数据访问层
 */
@ApplicationScoped
public class ToolExecutionLogRepository implements PanacheRepository<ToolExecutionLog> {

    /**
     * 根据工具 ID 查找最近的执行日志
     */
    public List<ToolExecutionLog> findByToolId(String toolId, int limit) {
        return find("toolId order by executedAt desc", toolId).page(0, limit).list();
    }

    /**
     * 根据任务 ID 查找执行日志
     */
    public List<ToolExecutionLog> findByTaskId(String taskId) {
        return find("taskId order by executedAt desc", taskId).list();
    }

    /**
     * 查找失败的执行日志
     */
    public List<ToolExecutionLog> findFailed() {
        return find("status", ToolExecutionStatus.FAILED).list();
    }

    /**
     * 查找失败的执行日志（按工具 ID）
     */
    public List<ToolExecutionLog> findFailedByToolId(String toolId) {
        return find("toolId = ?1 and status = ?2", toolId, ToolExecutionStatus.FAILED).list();
    }

    /**
     * 统计执行次数
     */
    public long countByToolId(String toolId) {
        return count("toolId", toolId);
    }

    /**
     * 统计成功次数
     */
    public long countSuccessByToolId(String toolId) {
        return count("toolId = ?1 and status = ?2", toolId, ToolExecutionStatus.SUCCESS);
    }

    /**
     * 查找指定时间范围内的执行日志
     */
    public List<ToolExecutionLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return find("executedAt between ?1 and ?2 order by executedAt desc", startTime, endTime).list();
    }

    /**
     * 清理指定时间之前的日志
     */
    public void cleanupBefore(LocalDateTime before) {
        delete("executedAt < ?1", before);
    }
}
