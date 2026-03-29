package com.easystation.logging.dto;

import java.io.Serializable;

/**
 * 日志统计信息 DTO
 */
public class LogStats implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long totalSize;
    private String totalSizeHuman;
    private int fileCount;
    private int archivedCount;
    private int activeFileCount;
    private String oldestFile;
    private String newestFile;
    
    public LogStats() {
    }
    
    public LogStats(long totalSize, int fileCount, int archivedCount, int activeFileCount, String oldestFile, String newestFile) {
        this.totalSize = totalSize;
        this.totalSizeHuman = formatFileSize(totalSize);
        this.fileCount = fileCount;
        this.archivedCount = archivedCount;
        this.activeFileCount = activeFileCount;
        this.oldestFile = oldestFile;
        this.newestFile = newestFile;
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    // Getters and Setters
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        this.totalSizeHuman = formatFileSize(totalSize);
    }
    
    public String getTotalSizeHuman() {
        return totalSizeHuman;
    }
    
    public void setTotalSizeHuman(String totalSizeHuman) {
        this.totalSizeHuman = totalSizeHuman;
    }
    
    public int getFileCount() {
        return fileCount;
    }
    
    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
    
    public int getArchivedCount() {
        return archivedCount;
    }
    
    public void setArchivedCount(int archivedCount) {
        this.archivedCount = archivedCount;
    }
    
    public int getActiveFileCount() {
        return activeFileCount;
    }
    
    public void setActiveFileCount(int activeFileCount) {
        this.activeFileCount = activeFileCount;
    }
    
    public String getOldestFile() {
        return oldestFile;
    }
    
    public void setOldestFile(String oldestFile) {
        this.oldestFile = oldestFile;
    }
    
    public String getNewestFile() {
        return newestFile;
    }
    
    public void setNewestFile(String newestFile) {
        this.newestFile = newestFile;
    }
}
