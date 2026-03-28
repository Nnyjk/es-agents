package com.easystation.agent.service;

import com.easystation.agent.domain.SystemEventLog;
import com.easystation.agent.dto.SystemEventLogDTO;
import com.easystation.agent.dto.SystemEventLogDTO.EventLogPage;
import com.easystation.agent.dto.SystemEventLogDTO.EventQueryCriteria;
import com.easystation.agent.repository.SystemEventLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统事件日志服务
 */
@Slf4j
@ApplicationScoped
public class SystemEventLogService {

    @Inject
    SystemEventLogRepository repository;

    /**
     * 记录事件
     */
    @Transactional
    public SystemEventLogDTO logEvent(SystemEventLogDTO dto) {
        SystemEventLog log = new SystemEventLog();
        log.eventType = dto.eventType();
        log.eventLevel = dto.eventLevel();
        log.module = dto.module();
        log.operation = dto.operation();
        log.targetType = dto.targetType();
        log.targetId = dto.targetId();
        log.userId = dto.userId();
        log.message = dto.message();
        log.details = dto.details();
        log.clientIp = dto.clientIp();
        log.requestPath = dto.requestPath();
        log.duration = dto.duration();
        log.errorMessage = dto.errorMessage();
        
        repository.persist(log);
        
        return toDTO(log);
    }

    /**
     * 查询事件日志（分页）
     */
    public EventLogPage queryEvents(EventQueryCriteria criteria, int page, int size) {
        List<SystemEventLog> logs = repository.findAllByFilters(criteria, page, size);
        long total = repository.countByFilters(criteria);
        
        List<SystemEventLogDTO> dtoList = logs.stream()
            .map(this::toDTO)
            .toList();
        
        int totalPages = (int) Math.ceil((double) total / size);
        
        return new EventLogPage(dtoList, total, page, size, totalPages);
    }

    /**
     * 获取单个事件详情
     */
    public SystemEventLogDTO getEventById(Long id) {
        SystemEventLog log = repository.findById(id);
        return log != null ? toDTO(log) : null;
    }

    /**
     * 清理旧日志
     * @param retentionDays 保留天数
     * @return 删除的记录数
     */
    @Transactional
    public long cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deleted = repository.deleteByCreatedAtBefore(cutoffDate);
        log.info("清理了 {} 条 {} 天前的系统事件日志", deleted, retentionDays);
        return deleted;
    }

    /**
     * 实体转 DTO
     */
    private SystemEventLogDTO toDTO(SystemEventLog log) {
        return new SystemEventLogDTO(
            log.id,
            log.eventType,
            log.eventLevel,
            log.module,
            log.operation,
            log.targetType,
            log.targetId,
            log.userId,
            log.message,
            log.details,
            log.clientIp,
            log.requestPath,
            log.duration,
            log.errorMessage,
            log.createdAt
        );
    }
}
