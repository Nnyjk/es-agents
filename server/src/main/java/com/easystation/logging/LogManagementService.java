package com.easystation.logging;

import com.easystation.logging.dto.LogFileInfo;
import com.easystation.logging.dto.LogStats;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 日志管理服务
 */
@ApplicationScoped
public class LogManagementService {
    
    private static final Logger LOG = Logger.getLogger(LogManagementService.class);
    
    private static final String LOG_DIR = "/var/log/easy-station";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    
    /**
     * 获取所有日志文件列表
     */
    public List<LogFileInfo> getLogFiles() {
        List<LogFileInfo> logFiles = new ArrayList<>();
        Path logDir = Paths.get(LOG_DIR);
        
        if (!Files.exists(logDir)) {
            LOG.warnf("日志目录不存在：%s", LOG_DIR);
            return logFiles;
        }
        
        try (Stream<Path> paths = Files.walk(logDir, 1)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".log") || path.toString().endsWith(".log.gz"))
                 .sorted(Comparator.comparingLong(p -> {
                     try {
                         return Files.getLastModifiedTime(p).toMillis();
                     } catch (IOException e) {
                         return 0L;
                     }
                 }))
                 .forEach(path -> {
                     try {
                         String fileName = path.getFileName().toString();
                         long fileSize = Files.size(path);
                         String lastModified = DATE_FORMATTER.format(Instant.ofEpochMilli(Files.getLastModifiedTime(path).toMillis()));
                         boolean isArchived = fileName.endsWith(".gz");
                         
                         logFiles.add(new LogFileInfo(fileName, fileSize, lastModified, isArchived, path.toString()));
                     } catch (IOException e) {
                         LOG.warnf("读取日志文件失败：%s - %s", path, e.getMessage());
                     }
                 });
        } catch (IOException e) {
            LOG.errorf("扫描日志目录失败：%s", e.getMessage());
        }
        
        return logFiles;
    }
    
    /**
     * 获取日志统计信息
     */
    public LogStats getLogStats() {
        List<LogFileInfo> logFiles = getLogFiles();
        
        long totalSize = logFiles.stream().mapToLong(LogFileInfo::getFileSize).sum();
        int fileCount = logFiles.size();
        int archivedCount = (int) logFiles.stream().filter(LogFileInfo::isArchived).count();
        int activeFileCount = fileCount - archivedCount;
        
        String oldestFile = logFiles.isEmpty() ? null : logFiles.get(0).getFileName();
        String newestFile = logFiles.isEmpty() ? null : logFiles.get(logFiles.size() - 1).getFileName();
        
        return new LogStats(totalSize, fileCount, archivedCount, activeFileCount, oldestFile, newestFile);
    }
    
    /**
     * 清理过期归档日志
     * @param retentionDays 保留天数
     * @return 删除的文件数量
     */
    public int cleanupOldArchives(int retentionDays) {
        Path logDir = Paths.get(LOG_DIR);
        
        if (!Files.exists(logDir)) {
            LOG.warnf("日志目录不存在：%s", LOG_DIR);
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;
        
        try (Stream<Path> paths = Files.walk(logDir, 1)) {
            List<Path> toDelete = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log.gz"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
            
            for (Path path : toDelete) {
                try {
                    Files.delete(path);
                    deletedCount++;
                    LOG.infof("删除过期归档日志：%s", path.getFileName());
                } catch (IOException e) {
                    LOG.errorf("删除归档日志失败：%s - %s", path, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.errorf("扫描日志目录失败：%s", e.getMessage());
        }
        
        LOG.infof("清理完成，删除 %d 个过期归档文件", deletedCount);
        return deletedCount;
    }
    
    /**
     * 手动触发日志轮转
     * 注意：实际轮转由 Logback 配置自动处理，此方法用于测试或强制轮转
     */
    public void triggerRotation() {
        LOG.info("手动触发日志轮转请求已接收");
        // 实际轮转由 Logback 的 SizeBasedTriggeringPolicy 和 TimeBasedTriggeringPolicy 自动处理
        // 这里仅记录日志，实际轮转会在下次日志写入时根据配置自动进行
        LOG.info("日志轮转将由 Logback 根据配置自动执行");
    }
}
