package com.easystation.export.repository;

import com.easystation.export.domain.ExportTask;
import com.easystation.export.enums.ExportStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ExportTaskRepository implements PanacheRepositoryBase<ExportTask, UUID> {

    /**
     * 根据用户 ID 查询导出任务列表
     */
    public List<ExportTask> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return find("userId = ?1 ORDER BY createdAt DESC", userId).list();
    }

    /**
     * 根据用户 ID 和状态查询导出任务列表
     */
    public List<ExportTask> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ExportStatus status) {
        return find("userId = ?1 AND status = ?2 ORDER BY createdAt DESC", userId, status).list();
    }

    /**
     * 根据 ID 和用户 ID 查询导出任务
     */
    public Optional<ExportTask> findByIdAndUserId(UUID id, UUID userId) {
        return find("id = ?1 AND userId = ?2", id, userId).firstResultOptional();
    }

    /**
     * 查询指定状态的任务
     */
    public List<ExportTask> findByStatus(ExportStatus status) {
        return find("status = ?1", status).list();
    }

    /**
     * 查询处理中的任务
     */
    public List<ExportTask> findProcessingTasks(LocalDateTime timeout) {
        return find("status = ?1 AND createdAt < ?2", ExportStatus.PROCESSING, timeout).list();
    }

    /**
     * 根据用户 ID 查询最近的导出任务
     */
    public List<ExportTask> findRecentByUserId(UUID userId, int limit) {
        return find("userId = ?1 ORDER BY createdAt DESC", userId).page(0, limit).list();
    }

    /**
     * 统计用户的任务数量
     */
    public long countByUserId(UUID userId) {
        return count("userId = ?1", userId);
    }

    /**
     * 统计用户指定状态的任务数量
     */
    public long countByUserIdAndStatus(UUID userId, ExportStatus status) {
        return count("userId = ?1 AND status = ?2", userId, status.name());
    }
}
