package com.easystation.audit.service;

import com.easystation.audit.domain.AuditLog;
import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuditLogService {

    public List<AuditRecord.Detail> list(AuditRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.username() != null && !query.username().isBlank()) {
            sql.append(" and username = :username");
            params.put("username", query.username());
        }
        if (query.userId() != null) {
            sql.append(" and userId = :userId");
            params.put("userId", query.userId());
        }
        if (query.action() != null) {
            sql.append(" and action = :action");
            params.put("action", query.action());
        }
        if (query.result() != null) {
            sql.append(" and result = :result");
            params.put("result", query.result());
        }
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            sql.append(" and resourceType = :resourceType");
            params.put("resourceType", query.resourceType());
        }
        if (query.resourceId() != null) {
            sql.append(" and resourceId = :resourceId");
            params.put("resourceId", query.resourceId());
        }
        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and (description like :keyword or details like :keyword)");
            params.put("keyword", "%" + query.keyword() + "%");
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return AuditLog.<AuditLog>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AuditRecord.Detail get(UUID id) {
        AuditLog log = AuditLog.findById(id);
        if (log == null) {
            return null;
        }
        return toDetail(log);
    }

    @Transactional
    public void record(AuditRecord.Create dto) {
        AuditLog auditLog = new AuditLog();
        auditLog.username = dto.username();
        auditLog.userId = dto.userId();
        auditLog.action = dto.action();
        auditLog.result = dto.result();
        auditLog.description = dto.description();
        auditLog.resourceType = dto.resourceType();
        auditLog.resourceId = dto.resourceId();
        auditLog.details = dto.details();
        auditLog.requestParams = dto.requestParams();
        auditLog.responseResult = dto.responseResult();
        auditLog.clientIp = dto.clientIp();
        auditLog.userAgent = dto.userAgent();
        auditLog.requestPath = dto.requestPath();
        auditLog.requestMethod = dto.requestMethod();
        auditLog.duration = dto.duration();
        auditLog.errorMessage = dto.errorMessage();
        auditLog.persist();

        Log.infof("Audit log recorded: %s by %s - %s", dto.action(), dto.username(), dto.result());
    }

    /**
     * 创建审计日志（别名方法，用于测试）
     * @param dto 审计记录
     * @return 创建的审计记录详情
     */
    @Transactional
    public AuditRecord.Detail create(AuditRecord.Create dto) {
        record(dto);
        // 通过查询获取刚创建的记录（使用最近的记录）
        AuditLog log = AuditLog.find("createdAt desc", io.quarkus.panache.common.Page.of(0, 1))
                .firstResult();
        return log != null ? toDetail(log) : null;
    }

    /**
     * 删除审计日志
     * @param id 审计日志 ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean delete(UUID id) {
        return AuditLog.deleteById(id);
    }

    /**
     * 快捷记录审计日志
     */
    @Transactional
    public void log(String username, UUID userId, AuditAction action, AuditResult result,
                    String description, String resourceType, UUID resourceId,
                    String clientIp, String requestPath) {
        AuditLog auditLog = new AuditLog();
        auditLog.username = username;
        auditLog.userId = userId;
        auditLog.action = action;
        auditLog.result = result;
        auditLog.description = description;
        auditLog.resourceType = resourceType;
        auditLog.resourceId = resourceId;
        auditLog.clientIp = clientIp;
        auditLog.requestPath = requestPath;
        auditLog.persist();
    }

    public AuditRecord.Summary getSummary() {
        long total = AuditLog.count();
        long successCount = AuditLog.count("result", AuditResult.SUCCESS);
        long failedCount = AuditLog.count("result", AuditResult.FAILED);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayCount = AuditLog.count("createdAt >= ?1 and createdAt <= ?2", todayStart, todayEnd);

        return new AuditRecord.Summary(total, successCount, failedCount, todayCount);
    }

    public Map<String, Long> countByAction(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime GROUP BY a.action", Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put(((AuditAction) row[0]).name(), (Long) row[1]);
        }
        return counts;
    }

    public Map<String, Long> countByUser(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery("SELECT a.username, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime GROUP BY a.username ORDER BY COUNT(a) DESC", Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setMaxResults(limit)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return counts;
    }

    private AuditRecord.Detail toDetail(AuditLog log) {
        return new AuditRecord.Detail(
                log.id,
                log.username,
                log.userId,
                log.action,
                log.result,
                log.description,
                log.resourceType,
                log.resourceId,
                log.details,
                log.requestParams,
                log.responseResult,
                log.clientIp,
                log.userAgent,
                log.requestPath,
                log.requestMethod,
                log.duration,
                log.errorMessage,
                log.createdAt
        );
    }

    // ==================== 导出功能 ====================

    public byte[] exportToJson(AuditRecord.ExportRequest request) {
        List<AuditRecord.Detail> logs = list(buildQueryFromExport(request));
        try {
            return io.vertx.core.json.Json.encodePrettily(logs).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.error("Failed to export audit logs to JSON", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    public byte[] exportToCsv(AuditRecord.ExportRequest request) {
        List<AuditRecord.Detail> logs = list(buildQueryFromExport(request));
        StringBuilder csv = new StringBuilder();
        csv.append("ID,用户名,操作类型,操作结果,描述,资源类型,资源ID,客户端IP,请求路径,请求方法,耗时(ms),创建时间\n");
        
        for (AuditRecord.Detail log : logs) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsv(log.id().toString()),
                    escapeCsv(log.username()),
                    escapeCsv(log.action() != null ? log.action().name() : ""),
                    escapeCsv(log.result() != null ? log.result().name() : ""),
                    escapeCsv(log.description()),
                    escapeCsv(log.resourceType() != null ? log.resourceType() : ""),
                    escapeCsv(log.resourceId() != null ? log.resourceId().toString() : ""),
                    escapeCsv(log.clientIp() != null ? log.clientIp() : ""),
                    escapeCsv(log.requestPath() != null ? log.requestPath() : ""),
                    escapeCsv(log.requestMethod() != null ? log.requestMethod() : ""),
                    log.duration() != null ? log.duration().toString() : "0",
                    log.createdAt() != null ? log.createdAt().toString() : ""
            ));
        }
        
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private AuditRecord.Query buildQueryFromExport(AuditRecord.ExportRequest request) {
        return new AuditRecord.Query(
                request.username(),
                request.userId(),
                request.action(),
                request.result(),
                request.resourceType(),
                null, // resourceId
                request.startTime(),
                request.endTime(),
                request.keyword(),
                null, // limit
                null  // offset
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== 统计功能 ====================

    public List<AuditRecord.StatisticsByUser> getStatisticsByUser(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        String sql = "SELECT username, userId, COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                "SUM(CASE WHEN result = 'FAILED' THEN 1 ELSE 0 END) as failed " +
                "FROM AuditLog WHERE createdAt BETWEEN :startTime AND :endTime " +
                "GROUP BY username, userId ORDER BY total DESC";
        
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery(sql, Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        return results.stream()
                .map(row -> new AuditRecord.StatisticsByUser(
                        (String) row[0],
                        (UUID) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue(),
                        ((Number) row[4]).longValue() * 100 / Math.max(1, ((Number) row[2]).longValue())
                ))
                .collect(Collectors.toList());
    }

    public List<AuditRecord.StatisticsByAction> getStatisticsByAction(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT action, COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                "SUM(CASE WHEN result = 'FAILED' THEN 1 ELSE 0 END) as failed " +
                "FROM AuditLog WHERE createdAt BETWEEN :startTime AND :endTime " +
                "GROUP BY action ORDER BY total DESC";

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery(sql, Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        return results.stream()
                .map(row -> new AuditRecord.StatisticsByAction(
                        (AuditAction) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<AuditRecord.StatisticsByDate> getStatisticsByDate(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT CAST(createdAt AS DATE) as date, COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                "SUM(CASE WHEN result = 'FAILED' THEN 1 ELSE 0 END) as failed " +
                "FROM AuditLog WHERE createdAt BETWEEN :startTime AND :endTime " +
                "GROUP BY CAST(createdAt AS DATE) ORDER BY date";

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery(sql, Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        return results.stream()
                .map(row -> new AuditRecord.StatisticsByDate(
                        ((java.sql.Date) row[0]).toLocalDate().atStartOfDay(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<AuditRecord.StatisticsByHour> getStatisticsByHour(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT HOUR(createdAt) as hour, COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                "SUM(CASE WHEN result = 'FAILED' THEN 1 ELSE 0 END) as failed " +
                "FROM AuditLog WHERE createdAt BETWEEN :startTime AND :endTime " +
                "GROUP BY HOUR(createdAt) ORDER BY hour";

        List<Object[]> results = AuditLog.getEntityManager()
                .createQuery(sql, Object[].class)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        return results.stream()
                .map(row -> new AuditRecord.StatisticsByHour(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public AuditRecord.StatisticsSummary getStatisticsSummary(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                "SUM(CASE WHEN result = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                "COUNT(DISTINCT username) as uniqueUsers, " +
                "COUNT(DISTINCT resourceId) as uniqueResources " +
                "FROM AuditLog WHERE createdAt BETWEEN :startTime AND :endTime";

        Object[] result = (Object[]) AuditLog.getEntityManager()
                .createQuery(sql)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getSingleResult();

        long total = ((Number) result[0]).longValue();
        long success = ((Number) result[1]).longValue();
        double successRate = total > 0 ? (success * 100.0 / total) : 0.0;

        return new AuditRecord.StatisticsSummary(
                total,
                success,
                ((Number) result[2]).longValue(),
                successRate,
                ((Number) result[3]).longValue(),
                ((Number) result[4]).longValue()
        );
    }

    // ==================== 归档功能 ====================

    @Transactional
    public AuditRecord.ArchiveResult archiveLogs(LocalDateTime beforeDate, Boolean includeSuccess, Boolean includeFailed) {
        StringBuilder sql = new StringBuilder("createdAt < :beforeDate");
        
        if (Boolean.TRUE.equals(includeSuccess) && !Boolean.TRUE.equals(includeFailed)) {
            sql.append(" AND result = 'SUCCESS'");
        } else if (!Boolean.TRUE.equals(includeSuccess) && Boolean.TRUE.equals(includeFailed)) {
            sql.append(" AND result = 'FAILED'");
        }
        // 如果都不指定或都指定，则不添加过滤条件

        List<AuditLog> logsToArchive = AuditLog.find(sql.toString(), 
                io.quarkus.panache.common.Parameters.with("beforeDate", beforeDate))
                .list();

        if (logsToArchive.isEmpty()) {
            return new AuditRecord.ArchiveResult(0, null, 0L);
        }

        // 生成归档文件名
        String archiveFilename = "audit_archive_" + 
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()) + 
                ".json";

        // 转换为 JSON 并存储（这里简化为模拟存储）
        List<AuditRecord.Detail> details = logsToArchive.stream()
                .map(this::toDetail)
                .collect(Collectors.toList());

        byte[] archiveData = io.vertx.core.json.Json.encodePrettily(details).getBytes();

        // 删除已归档的日志
        for (AuditLog log : logsToArchive) {
            log.delete();
        }

        return new AuditRecord.ArchiveResult(
                logsToArchive.size(),
                archiveFilename,
                (long) archiveData.length
        );
    }

    // ==================== 清理功能 ====================

    @Transactional
    public AuditRecord.CleanupResult cleanupLogs(LocalDateTime beforeDate, Boolean dryRun) {
        long count = AuditLog.count("createdAt < ?1", beforeDate);
        
        if (Boolean.TRUE.equals(dryRun)) {
            return new AuditRecord.CleanupResult(Math.toIntExact(count), beforeDate);
        }

        int deleted = Math.toIntExact(AuditLog.delete("createdAt < ?1", beforeDate));
        return new AuditRecord.CleanupResult(deleted, beforeDate);
    }
}